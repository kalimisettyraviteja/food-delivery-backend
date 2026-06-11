package com.fooddelivery.userservice.service;

import com.fooddelivery.userservice.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    UserResponse registerUser(RegisterRequest request);

    LoginResponse loginUser(LoginRequest request);

    List<UserResponse> getAllUsers();

    UserResponse getProfile(Long userId);

    UserResponse updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    UserResponse updateProfilePhoto(Long userId, MultipartFile file);

    ResponseEntity<byte[]> getProfilePhoto(Long userId);

    void removeProfilePhoto(Long userId);

}