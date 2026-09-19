package com.fooddelivery.userservice.service;

import com.fooddelivery.userservice.config.JwtUtil;
import com.fooddelivery.userservice.dto.*;
import com.fooddelivery.userservice.entity.*;
import com.fooddelivery.userservice.enums.AccountStatus;
import com.fooddelivery.userservice.enums.ManagerRegistrationStatus;
import com.fooddelivery.userservice.enums.Role;
import com.fooddelivery.userservice.repository.ManagerRegistrationRequestRepository;
import com.fooddelivery.userservice.repository.OtpTokenRepository;
import com.fooddelivery.userservice.repository.PendingRegistrationRepository;
import com.fooddelivery.userservice.repository.SavedAddressRepository;
import com.fooddelivery.userservice.repository.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class UserServiceImpl implements UserService {

    private static final String EMAIL_VERIFICATION = "EMAIL_VERIFICATION";
    private static final String PASSWORD_RESET = "PASSWORD_RESET";
    private static final String ACCOUNT_DEACTIVATION = "ACCOUNT_DEACTIVATION";
    private static final String ACCOUNT_REACTIVATION = "ACCOUNT_REACTIVATION";

    private static final String TEMP_PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#";


    private final UserRepository userRepository;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final ManagerRegistrationRequestRepository managerRegistrationRequestRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final SavedAddressRepository savedAddressRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public UserServiceImpl(UserRepository userRepository,
                           PendingRegistrationRepository pendingRegistrationRepository,
                           ManagerRegistrationRequestRepository managerRegistrationRequestRepository,
                           OtpTokenRepository otpTokenRepository,
                           SavedAddressRepository savedAddressRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           EmailService emailService) {
        this.userRepository = userRepository;
        this.pendingRegistrationRepository = pendingRegistrationRepository;
        this.managerRegistrationRequestRepository = managerRegistrationRequestRepository;
        this.otpTokenRepository = otpTokenRepository;
        this.savedAddressRepository = savedAddressRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    @Override
    public EmailStatusResponse checkEmailStatus(String email) {
        String normalizedEmail = normalizeEmail(email);

        User existingUser = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElse(null);

        if (existingUser != null) {
            if (existingUser.getAccountStatus() == AccountStatus.DEACTIVATED) {
                return EmailStatusResponse.builder()
                        .message("This account is deactivated. Reactivate it using a one-time password.")
                        .nextStep("REACTIVATION")
                        .email(normalizedEmail)
                        .name(existingUser.getName())
                        .build();
            }

            return EmailStatusResponse.builder()
                    .message("Continue with the next step.")
                    .nextStep("LOGIN")
                    .email(normalizedEmail)
                    .name(existingUser.getName())
                    .build();
        }

        if (pendingRegistrationRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            PendingRegistration pending = pendingRegistrationRepository.findByEmailIgnoreCase(normalizedEmail).orElseThrow();
            return EmailStatusResponse.builder()
                    .message("Continue with the next step.")
                    .nextStep("PENDING_VERIFICATION")
                    .email(normalizedEmail)
                    .name(pending.getName())
                    .build();
        }

        return managerRegistrationRequestRepository.findByEmailIgnoreCase(normalizedEmail)
                .map(request -> {
                    if (request.getStatus() == ManagerRegistrationStatus.PENDING) {
                        return EmailStatusResponse.builder()
                                .message("Your manager registration request is under review.")
                                .nextStep("MANAGER_REQUEST_PENDING")
                                .email(normalizedEmail)
                                .name(request.getName())
                                .build();
                    }
                    if (request.getStatus() == ManagerRegistrationStatus.REJECTED) {
                        return EmailStatusResponse.builder()
                                .message("Your previous manager request was rejected. You can submit again.")
                                .nextStep("MANAGER_REQUEST_REJECTED")
                                .email(normalizedEmail)
                                .name(request.getName())
                                .build();
                    }
                    return EmailStatusResponse.builder()
                            .message("Continue with the next step.")
                            .nextStep("REGISTER")
                            .email(normalizedEmail)
                            .build();
                })
                .orElseGet(() -> EmailStatusResponse.builder()
                        .message("Continue with the next step.")
                        .nextStep("REGISTER")
                        .email(normalizedEmail)
                        .build());
    }

    @Override
    @Transactional
    public void registerUser(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Password and confirm password do not match.");
        }

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new RuntimeException("An account with this email already exists.");
        }

        managerRegistrationRequestRepository.findByEmailIgnoreCase(normalizedEmail).ifPresent(existing -> {
            if (existing.getStatus() == ManagerRegistrationStatus.PENDING) {
                throw new RuntimeException("A manager registration request is already pending for this email.");
            }
        });

        PendingRegistration pendingRegistration = pendingRegistrationRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseGet(PendingRegistration::new);

        pendingRegistration.setName(request.getName().trim());
        pendingRegistration.setEmail(normalizedEmail);
        pendingRegistration.setPassword(passwordEncoder.encode(request.getPassword()));
        pendingRegistration.setCreatedAt(LocalDateTime.now());

        pendingRegistrationRepository.save(pendingRegistration);

        sendOtp(normalizedEmail, pendingRegistration.getName(), EMAIL_VERIFICATION);
    }

    @Override
    @Transactional
    public LoginResponse verifyEmailAndLogin(String email, String otp) {
        String normalizedEmail = normalizeEmail(email);

        OtpToken token = otpTokenRepository.findByEmailAndOtpAndTypeAndUsedFalse(normalizedEmail, otp, EMAIL_VERIFICATION)
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP. Please try again."));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired OTP. Please try again.");
        }

        PendingRegistration pendingRegistration = pendingRegistrationRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("Registration session expired. Please try again."));

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new RuntimeException("Account already exists. Please sign in.");
        }

        User user = User.builder()
                .name(pendingRegistration.getName())
                .email(pendingRegistration.getEmail())
                .password(pendingRegistration.getPassword())
                .phone(null)
                .role(Role.USER)
                .accountStatus(AccountStatus.ACTIVE)
                .mustChangePassword(false)
                .build();

        User savedUser = userRepository.save(user);

        token.setUsed(true);
        otpTokenRepository.save(token);

        pendingRegistrationRepository.deleteByEmail(normalizedEmail);
        otpTokenRepository.deleteByEmailAndType(normalizedEmail, EMAIL_VERIFICATION);

        String jwt = jwtUtil.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name());

        return new LoginResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                jwt,
                savedUser.getMustChangePassword()
        );
    }

    @Override
    @Transactional
    public void resendVerificationOtp(String email) {
        String normalizedEmail = normalizeEmail(email);

        PendingRegistration pendingRegistration = pendingRegistrationRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("Pending registration not found."));

        sendOtp(normalizedEmail, pendingRegistration.getName(), EMAIL_VERIFICATION);
    }

    @Override
    public LoginResponse loginUser(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("Invalid credentials."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials.");
        }

        validateAccountForLogin(user);

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                token,
                user.getMustChangePassword()
        );
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        String normalizedEmail = normalizeEmail(email);

        userRepository.findByEmailIgnoreCase(normalizedEmail).ifPresent(user -> {
            if (user.getAccountStatus() == AccountStatus.ACTIVE) {
                sendOtp(normalizedEmail, user.getName(), PASSWORD_RESET);
            }
        });
    }

    @Override
    public void verifyResetOtp(String email, String otp) {
        String normalizedEmail = normalizeEmail(email);

        OtpToken token = otpTokenRepository.findByEmailAndOtpAndTypeAndUsedFalse(normalizedEmail, otp, PASSWORD_RESET)
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP. Please try again."));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired OTP. Please try again.");
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match.");
        }

        OtpToken token = otpTokenRepository.findByEmailAndOtpAndTypeAndUsedFalse(
                        normalizedEmail, request.getOtp(), PASSWORD_RESET)
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP. Please try again."));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired OTP. Please try again.");
        }

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("Unable to reset password."));

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("New password must be different from the old password.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);

        token.setUsed(true);
        otpTokenRepository.save(token);
        otpTokenRepository.deleteByEmailAndType(normalizedEmail, PASSWORD_RESET);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    @Override
    public List<UserResponse> getApprovedManagers() {
        return userRepository.findByRoleAndAccountStatusOrderByNameAsc(Role.RESTAURANT_MANAGER, AccountStatus.ACTIVE)
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    @Override
    @Transactional
    public void submitManagerRegistrationRequest(ManagerRegistrationSubmitRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new RuntimeException("An account with this email already exists.");
        }

        if (pendingRegistrationRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new RuntimeException("This email is already used for a pending customer registration.");
        }

        ManagerRegistrationRequest existing = managerRegistrationRequestRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElse(null);

        if (existing != null && existing.getStatus() == ManagerRegistrationStatus.PENDING) {
            throw new RuntimeException("A manager registration request is already pending for this email.");
        }

        if (existing != null && existing.getStatus() == ManagerRegistrationStatus.APPROVED) {
            throw new RuntimeException("This manager request was already approved.");
        }

        ManagerRegistrationRequest entity = existing != null ? existing : new ManagerRegistrationRequest();
        entity.setName(request.getName().trim());
        entity.setEmail(normalizedEmail);
        entity.setPhone(request.getPhone().trim());
        entity.setRequestedRole(Role.RESTAURANT_MANAGER);
        entity.setStatus(ManagerRegistrationStatus.PENDING);
        entity.setRejectionReason(null);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setReviewedAt(null);

        managerRegistrationRequestRepository.save(entity);
    }

    @Override
    public List<ManagerRegistrationResponse> getManagerRegistrationRequests(ManagerRegistrationStatus status) {
        List<ManagerRegistrationRequest> requests = status == null
                ? managerRegistrationRequestRepository.findAllByOrderByCreatedAtDesc()
                : managerRegistrationRequestRepository.findByStatusOrderByCreatedAtDesc(status);

        return requests.stream()
                .map(this::mapToManagerRegistrationResponse)
                .toList();
    }

    @Override
    @Transactional
    public ManagerRegistrationResponse approveManagerRegistration(Long requestId) {
        ManagerRegistrationRequest request = managerRegistrationRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Manager registration request not found."));

        if (request.getStatus() != ManagerRegistrationStatus.PENDING) {
            throw new RuntimeException("Only pending requests can be approved.");
        }

        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new RuntimeException("A user already exists with this email.");
        }

        String temporaryPassword = generateTemporaryPassword();

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(temporaryPassword))
                .role(Role.RESTAURANT_MANAGER)
                .accountStatus(AccountStatus.ACTIVE)
                .mustChangePassword(true)
                .build();

        userRepository.save(user);

        request.setStatus(ManagerRegistrationStatus.APPROVED);
        request.setRejectionReason(null);
        request.setReviewedAt(LocalDateTime.now());
        managerRegistrationRequestRepository.save(request);

        emailService.sendManagerApprovalCredentials(request.getEmail(), request.getName(), temporaryPassword);

        return mapToManagerRegistrationResponse(request);
    }

    @Override
    @Transactional
    public ManagerRegistrationResponse rejectManagerRegistration(Long requestId, ManagerRegistrationDecisionRequest dto) {
        ManagerRegistrationRequest request = managerRegistrationRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Manager registration request not found."));

        if (request.getStatus() == ManagerRegistrationStatus.APPROVED) {
            throw new RuntimeException("Approved requests cannot be rejected.");
        }

        String rejectionReason = dto != null ? dto.getRejectionReason() : null;
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new RuntimeException("Rejection reason is required.");
        }

        request.setStatus(ManagerRegistrationStatus.REJECTED);
        request.setRejectionReason(rejectionReason.trim());
        request.setReviewedAt(LocalDateTime.now());

        return mapToManagerRegistrationResponse(managerRegistrationRequestRepository.save(request));
    }

    @Override
    public UserResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));
        return mapToUserResponse(user);
    }

    @Override
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        user.setName(request.getName());
        user.setPhone(request.getPhone());

        return mapToUserResponse(userRepository.save(user));
    }

    /*@Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (user.getMustChangePassword() != true) {
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new RuntimeException("Current password is incorrect.");
            }
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match.");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("New password must be different from the old password.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);
    }*/


    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        boolean mustChangePassword =
                Boolean.TRUE.equals(user.getMustChangePassword());

        // Normal password change requires the current password.
        if (!mustChangePassword) {
            if (request.getCurrentPassword() == null ||
                    request.getCurrentPassword().isBlank()) {
                throw new RuntimeException("Current password is required.");
            }

            if (!passwordEncoder.matches(
                    request.getCurrentPassword(),
                    user.getPassword())) {
                throw new RuntimeException("Current password is incorrect.");
            }
        }

        if (request.getNewPassword() == null ||
                request.getConfirmPassword() == null ||
                !request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException(
                    "New password and confirm password do not match.");
        }

        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword())) {
            throw new RuntimeException(
                    "New password must be different from the old password.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);

        userRepository.save(user);
    }


    @Override
    @Transactional
    public void requestAccountDeactivation(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Only active accounts can be deactivated.");
        }

        sendOtp(user.getEmail(), user.getName(), ACCOUNT_DEACTIVATION);
    }

    @Override
    @Transactional
    public void confirmAccountDeactivation(Long userId, String otp) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Only active accounts can be deactivated.");
        }

        OtpToken token = otpTokenRepository.findByEmailAndOtpAndTypeAndUsedFalse(
                        user.getEmail(), otp, ACCOUNT_DEACTIVATION)
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP. Please try again."));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired OTP. Please try again.");
        }

        user.setAccountStatus(AccountStatus.DEACTIVATED);
        userRepository.save(user);

        token.setUsed(true);
        otpTokenRepository.save(token);
        otpTokenRepository.deleteByEmailAndType(user.getEmail(), ACCOUNT_DEACTIVATION);
    }

    @Override
    @Transactional
    public void requestAccountReactivation(String email) {
        String normalizedEmail = normalizeEmail(email);

        userRepository.findByEmailIgnoreCase(normalizedEmail).ifPresent(user -> {
            if (user.getAccountStatus() == AccountStatus.DEACTIVATED) {
                sendOtp(normalizedEmail, user.getName(), ACCOUNT_REACTIVATION);
            }
        });
    }

    @Override
    @Transactional
    public LoginResponse confirmAccountReactivation(String email, String otp) {
        String normalizedEmail = normalizeEmail(email);

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP. Please try again."));

        if (user.getAccountStatus() != AccountStatus.DEACTIVATED) {
            throw new RuntimeException("This account is not deactivated.");
        }

        OtpToken token = otpTokenRepository.findByEmailAndOtpAndTypeAndUsedFalse(
                        normalizedEmail, otp, ACCOUNT_REACTIVATION)
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP. Please try again."));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired OTP. Please try again.");
        }

        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);

        token.setUsed(true);
        otpTokenRepository.save(token);
        otpTokenRepository.deleteByEmailAndType(normalizedEmail, ACCOUNT_REACTIVATION);

        String jwt = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                jwt,
                user.getMustChangePassword()
        );
    }

    @Override
    public UserResponse updateProfilePhoto(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Please select an image.");
        }

        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("image/jpeg")
                        && !contentType.equals("image/png")
                        && !contentType.equals("image/jpg")
                        && !contentType.equals("image/webp"))) {
            throw new RuntimeException("Only JPG, PNG, and WEBP images are allowed.");
        }

        try {
            user.setProfilePhoto(file.getBytes());
            user.setProfilePhotoContentType(contentType);
            return mapToUserResponse(userRepository.save(user));
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload profile photo.");
        }
    }

    @Override
    public ResponseEntity<byte[]> getProfilePhoto(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (user.getProfilePhoto() == null || user.getProfilePhoto().length == 0) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(user.getProfilePhotoContentType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"profile-photo\"")
                .body(user.getProfilePhoto());
    }

    @Override
    public void removeProfilePhoto(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));
        user.setProfilePhoto(null);
        user.setProfilePhotoContentType(null);
        userRepository.save(user);
    }

    @Override
    public List<SavedAddressResponse> getMyAddresses(Long userId) {
        return savedAddressRepository.findByUserIdOrderByIsDefaultDescIdDesc(userId)
                .stream()
                .map(this::mapToSavedAddressResponse)
                .toList();
    }

    @Override
    public SavedAddressResponse getDefaultAddress(Long userId) {
        SavedAddress address = savedAddressRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElseThrow(() -> new RuntimeException("Default address not found."));
        return mapToSavedAddressResponse(address);
    }

    @Override
    @Transactional
    public SavedAddressResponse addAddress(Long userId, SavedAddressRequest request) {
        boolean isFirstAddress = savedAddressRepository.countByUserId(userId) == 0;
        boolean shouldBeDefault = isFirstAddress || Boolean.TRUE.equals(request.getIsDefault());

        if (shouldBeDefault) {
            clearDefaultAddress(userId);
        }

        String customLabel = normalizeCustomLabel(request);

        SavedAddress address = SavedAddress.builder()
                .userId(userId)
                .label(request.getLabel())
                .customLabel(customLabel)
                .receiverName(request.getReceiverName())
                .phoneNumber(request.getPhoneNumber())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .landmark(request.getLandmark())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .isDefault(shouldBeDefault)
                .build();

        return mapToSavedAddressResponse(savedAddressRepository.save(address));
    }

    @Override
    @Transactional
    public SavedAddressResponse updateAddress(Long userId, Long addressId, SavedAddressRequest request) {
        SavedAddress address = savedAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found."));

        boolean shouldBeDefault = Boolean.TRUE.equals(request.getIsDefault());

        if (shouldBeDefault) {
            clearDefaultAddress(userId);
        }

        String customLabel = normalizeCustomLabel(request);

        address.setLabel(request.getLabel());
        address.setCustomLabel(customLabel);
        address.setReceiverName(request.getReceiverName());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setLandmark(request.getLandmark());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());
        address.setIsDefault(shouldBeDefault || Boolean.TRUE.equals(address.getIsDefault()));

        SavedAddress updated = savedAddressRepository.save(address);

        if (!shouldBeDefault && savedAddressRepository.findByUserIdAndIsDefaultTrue(userId).isEmpty()) {
            updated.setIsDefault(true);
            updated = savedAddressRepository.save(updated);
        }

        return mapToSavedAddressResponse(updated);
    }

    @Override
    @Transactional
    public SavedAddressResponse setDefaultAddress(Long userId, Long addressId) {
        SavedAddress address = savedAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found."));

        clearDefaultAddress(userId);
        address.setIsDefault(true);

        return mapToSavedAddressResponse(savedAddressRepository.save(address));
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        SavedAddress address = savedAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found."));

        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());
        savedAddressRepository.delete(address);

        if (wasDefault) {
            savedAddressRepository.findByUserIdOrderByIsDefaultDescIdDesc(userId)
                    .stream()
                    .findFirst()
                    .ifPresent(next -> {
                        next.setIsDefault(true);
                        savedAddressRepository.save(next);
                    });
        }
    }

    private void clearDefaultAddress(Long userId) {
        List<SavedAddress> addresses = savedAddressRepository.findByUserId(userId);
        for (SavedAddress address : addresses) {
            if (Boolean.TRUE.equals(address.getIsDefault())) {
                address.setIsDefault(false);
            }
        }
        savedAddressRepository.saveAll(addresses);
    }

    /*private void sendOtp(String email, String name, String type) {
        otpTokenRepository.deleteByEmailAndType(email, type);

        String otp = String.format("%06d", new Random().nextInt(1_000_000));

        OtpToken token = OtpToken.builder()
                .email(email)
                .otp(otp)
                .type(type)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        otpTokenRepository.save(token);

        if (EMAIL_VERIFICATION.equals(type)) {
            emailService.sendVerificationOtp(email, name, otp);
        } else {
            emailService.sendPasswordResetOtp(email, name, otp);
        }
    }
*/
    private void sendOtp(String email, String name, String type) {
        otpTokenRepository.deleteByEmailAndType(email, type);

        String otp = String.format("%06d", new Random().nextInt(1_000_000));

        OtpToken token = OtpToken.builder()
                .email(email)
                .otp(otp)
                .type(type)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        otpTokenRepository.save(token);

        switch (type) {
            case EMAIL_VERIFICATION -> emailService.sendVerificationOtp(email, name, otp);
            case PASSWORD_RESET -> emailService.sendPasswordResetOtp(email, name, otp);
            case ACCOUNT_DEACTIVATION -> emailService.sendDeactivationOtp(email, name, otp);
            case ACCOUNT_REACTIVATION -> emailService.sendReactivationOtp(email, name, otp);
            default -> throw new IllegalArgumentException("Unknown OTP type: " + type);
        }
    }

    private void validateAccountForLogin(User user) {
        if (user.getAccountStatus() == AccountStatus.ACTIVE) {
            return;
        }

        if (user.getAccountStatus() == AccountStatus.DEACTIVATED) {
            throw new RuntimeException(
                    "Your account is deactivated. Please reactivate it using OTP."
            );
        }

        if (user.getAccountStatus() == AccountStatus.SUSPENDED) {
            throw new RuntimeException("Your account is suspended. Please contact admin.");
        }

        if (user.getAccountStatus() == AccountStatus.REJECTED) {
            throw new RuntimeException("Your account has been rejected.");
        }

        throw new RuntimeException("Your account is not active yet.");
    }

    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(TEMP_PASSWORD_CHARS.charAt(random.nextInt(TEMP_PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim();
    }

    private String normalizeCustomLabel(SavedAddressRequest request) {
        if (request.getLabel() == AddressLabel.OTHER) {
            if (request.getCustomLabel() == null || request.getCustomLabel().trim().isEmpty()) {
                throw new RuntimeException("Custom label is required when address label is OTHER.");
            }
            return request.getCustomLabel().trim();
        }
        return null;
    }

    private UserResponse mapToUserResponse(User user) {
        String profilePhotoUrl = null;
        if (user.getProfilePhoto() != null && user.getProfilePhoto().length > 0) {
            profilePhotoUrl = "/api/users/" + user.getId() + "/profile-photo";
        }

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .accountStatus(user.getAccountStatus().name())
                .mustChangePassword(user.getMustChangePassword())
                .profilePhotoUrl(profilePhotoUrl)
                .build();
    }

    private ManagerRegistrationResponse mapToManagerRegistrationResponse(ManagerRegistrationRequest request) {
        return ManagerRegistrationResponse.builder()
                .id(request.getId())
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .requestedRole(request.getRequestedRole().name())
                .status(request.getStatus().name())
                .rejectionReason(request.getRejectionReason())
                .createdAt(request.getCreatedAt())
                .reviewedAt(request.getReviewedAt())
                .build();
    }

    private SavedAddressResponse mapToSavedAddressResponse(SavedAddress address) {
        return SavedAddressResponse.builder()
                .id(address.getId())
                .label(address.getLabel())
                .customLabel(address.getCustomLabel())
                .receiverName(address.getReceiverName())
                .phoneNumber(address.getPhoneNumber())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .landmark(address.getLandmark())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .isDefault(address.getIsDefault())
                .build();
    }
}