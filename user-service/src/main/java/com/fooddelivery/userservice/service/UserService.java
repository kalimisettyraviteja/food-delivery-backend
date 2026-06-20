package com.fooddelivery.userservice.service;

import com.fooddelivery.userservice.dto.*;
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

    UserResponse getProfile(Long userId);

    UserResponse updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    UserResponse updateProfilePhoto(Long userId, MultipartFile file);

    ResponseEntity<byte[]> getProfilePhoto(Long userId);

    void removeProfilePhoto(Long userId);
}