package com.fooddelivery.userservice.service;

import com.fooddelivery.userservice.dto.*;
import com.fooddelivery.userservice.enums.ManagerRegistrationStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    EmailStatusResponse checkEmailStatus(String email);

    void registerUser(RegisterRequest request);

    LoginResponse verifyEmailAndLogin(String email, String otp);

    void resendVerificationOtp(String email);

    LoginResponse loginUser(LoginRequest request);

    void forgotPassword(String email);

    void verifyResetOtp(String email, String otp);

    void resetPassword(ResetPasswordRequest request);

    List<UserResponse> getAllUsers();

    List<UserResponse> getApprovedManagers();

    void submitManagerRegistrationRequest(ManagerRegistrationSubmitRequest request);

    List<ManagerRegistrationResponse> getManagerRegistrationRequests(ManagerRegistrationStatus status);

    ManagerRegistrationResponse approveManagerRegistration(Long requestId);

    ManagerRegistrationResponse rejectManagerRegistration(Long requestId, ManagerRegistrationDecisionRequest request);

    UserResponse getProfile(Long userId);

    UserResponse updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    UserResponse updateProfilePhoto(Long userId, MultipartFile file);

    ResponseEntity<byte[]> getProfilePhoto(Long userId);

    void removeProfilePhoto(Long userId);

    List<SavedAddressResponse> getMyAddresses(Long userId);

    SavedAddressResponse getDefaultAddress(Long userId);

    SavedAddressResponse addAddress(Long userId, SavedAddressRequest request);

    SavedAddressResponse updateAddress(Long userId, Long addressId, SavedAddressRequest request);

    SavedAddressResponse setDefaultAddress(Long userId, Long addressId);

    void deleteAddress(Long userId, Long addressId);

    void requestAccountDeactivation(Long userId);

    void confirmAccountDeactivation(Long userId, String otp);

    void requestAccountReactivation(String email);

    LoginResponse confirmAccountReactivation(String email, String otp);

}