package com.fooddelivery.userservice.service;

import com.fooddelivery.userservice.dto.LoginRequest;
import com.fooddelivery.userservice.dto.LoginResponse;
import com.fooddelivery.userservice.dto.RegisterRequest;
import com.fooddelivery.userservice.dto.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse registerUser(RegisterRequest request);

    LoginResponse loginUser(LoginRequest request);

    List<UserResponse> getAllUsers();
}