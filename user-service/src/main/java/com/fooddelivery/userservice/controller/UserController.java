package com.fooddelivery.userservice.controller;

import com.fooddelivery.userservice.dto.LoginRequest;
import com.fooddelivery.userservice.dto.LoginResponse;
import com.fooddelivery.userservice.dto.RegisterRequest;
import com.fooddelivery.userservice.dto.UserResponse;
import com.fooddelivery.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.registerUser(request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/login")                                           // ✅ added
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.loginUser(request));
    }


    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}