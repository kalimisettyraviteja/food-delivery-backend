package com.fooddelivery.userservice.service;

import com.fooddelivery.userservice.config.JwtUtil;
import com.fooddelivery.userservice.dto.*;
import com.fooddelivery.userservice.entity.OtpToken;
import com.fooddelivery.userservice.entity.PendingRegistration;
import com.fooddelivery.userservice.entity.User;
import com.fooddelivery.userservice.repository.OtpTokenRepository;
import com.fooddelivery.userservice.repository.PendingRegistrationRepository;
import com.fooddelivery.userservice.repository.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class UserServiceImpl implements UserService {

    private static final String EMAIL_VERIFICATION = "EMAIL_VERIFICATION";
    private static final String PASSWORD_RESET = "PASSWORD_RESET";

    private final UserRepository userRepository;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public UserServiceImpl(UserRepository userRepository,
                           PendingRegistrationRepository pendingRegistrationRepository,
                           OtpTokenRepository otpTokenRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           EmailService emailService) {
        this.userRepository = userRepository;
        this.pendingRegistrationRepository = pendingRegistrationRepository;
        this.otpTokenRepository = otpTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    @Override
    public EmailStatusResponse checkEmailStatus(String email) {
        if (userRepository.existsByEmail(email)) {
            return EmailStatusResponse.builder()
                    .message("Continue with the next step.")
                    .nextStep("LOGIN")
                    .email(email)
                    .build();
        }

        return pendingRegistrationRepository.findByEmail(email)
                .map(pending -> EmailStatusResponse.builder()
                        .message("Continue with the next step.")
                        .nextStep("PENDING_VERIFICATION")
                        .email(email)
                        .name(pending.getName())
                        .build())
                .orElseGet(() -> EmailStatusResponse.builder()
                        .message("Continue with the next step.")
                        .nextStep("REGISTER")
                        .email(email)
                        .build());
    }
    @Override
    @Transactional
    public void registerUser(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Password and confirm password do not match.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("An account with this email already exists.");
        }

        PendingRegistration pendingRegistration = pendingRegistrationRepository.findByEmail(request.getEmail())
                .orElseGet(PendingRegistration::new);

        pendingRegistration.setName(request.getName());
        pendingRegistration.setEmail(request.getEmail());
        pendingRegistration.setPassword(passwordEncoder.encode(request.getPassword()));

        if (pendingRegistration.getCreatedAt() == null) {
            pendingRegistration.setCreatedAt(LocalDateTime.now());
        } else {
            pendingRegistration.setCreatedAt(LocalDateTime.now());
        }

        pendingRegistrationRepository.save(pendingRegistration);

        sendOtp(request.getEmail(), request.getName(), EMAIL_VERIFICATION);
    }
 /*   @Override
    @Transactional
    public void registerUser(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Password and confirm password do not match.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("An account with this email already exists.");
        }

        pendingRegistrationRepository.deleteByEmail(request.getEmail());

        PendingRegistration pendingRegistration = PendingRegistration.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .createdAt(LocalDateTime.now())
                .build();

        pendingRegistrationRepository.save(pendingRegistration);
        sendOtp(request.getEmail(), request.getName(), EMAIL_VERIFICATION);
    }*/

    @Override
    @Transactional
    public LoginResponse verifyEmailAndLogin(String email, String otp) {
        OtpToken token = otpTokenRepository.findByEmailAndOtpAndTypeAndUsedFalse(email, otp, EMAIL_VERIFICATION)
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP. Please try again."));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired OTP. Please try again.");
        }

        PendingRegistration pendingRegistration = pendingRegistrationRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Registration session expired. Please try again."));

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Account already exists. Please sign in.");
        }

        User user = User.builder()
                .name(pendingRegistration.getName())
                .email(pendingRegistration.getEmail())
                .password(pendingRegistration.getPassword())
                .phone(null)
                .role("USER")
                .build();

        User savedUser = userRepository.save(user);

        token.setUsed(true);
        otpTokenRepository.save(token);

        pendingRegistrationRepository.deleteByEmail(email);
        otpTokenRepository.deleteByEmailAndType(email, EMAIL_VERIFICATION);

        String jwt = jwtUtil.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());

        return new LoginResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                jwt
        );
    }

    @Override
    @Transactional
    public void resendVerificationOtp(String email) {
        PendingRegistration pendingRegistration = pendingRegistrationRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Pending registration not found."));

        sendOtp(email, pendingRegistration.getName(), EMAIL_VERIFICATION);
    }

    @Override
    public LoginResponse loginUser(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials.");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());

        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                token
        );
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user ->
                sendOtp(email, user.getName(), PASSWORD_RESET)
        );
    }

    @Override
    public void verifyResetOtp(String email, String otp) {
        OtpToken token = otpTokenRepository.findByEmailAndOtpAndTypeAndUsedFalse(email, otp, PASSWORD_RESET)
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP. Please try again."));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired OTP. Please try again.");
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match.");
        }

        OtpToken token = otpTokenRepository.findByEmailAndOtpAndTypeAndUsedFalse(
                        request.getEmail(), request.getOtp(), PASSWORD_RESET)
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP. Please try again."));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired OTP. Please try again.");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Unable to reset password."));

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("New password must be different from the old password.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        token.setUsed(true);
        otpTokenRepository.save(token);
        otpTokenRepository.deleteByEmailAndType(request.getEmail(), PASSWORD_RESET);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponse)
                .toList();
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

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect.");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("New password must be different from the old password.");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
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

        if (EMAIL_VERIFICATION.equals(type)) {
            emailService.sendVerificationOtp(email, name, otp);
        } else {
            emailService.sendPasswordResetOtp(email, name, otp);
        }
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
                .role(user.getRole())
                .profilePhotoUrl(profilePhotoUrl)
                .build();
    }
}