package com.fooddelivery.userservice.controller;

import com.fooddelivery.userservice.dto.*;
import com.fooddelivery.userservice.enums.ManagerRegistrationStatus;
import com.fooddelivery.userservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Auth
    @PostMapping("/auth/email-status")
    public ResponseEntity<EmailStatusResponse> checkEmailStatus(@Valid @RequestBody EmailCheckRequest request) {
        return ResponseEntity.ok(userService.checkEmailStatus(request.getEmail()));
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        userService.registerUser(request);
        return ResponseEntity.ok("Verification OTP sent successfully.");
    }

    @PostMapping("/manager/register-request")
    public ResponseEntity<String> submitManagerRegistrationRequest(
            @Valid @RequestBody ManagerRegistrationSubmitRequest request) {
        userService.submitManagerRegistrationRequest(request);
        return ResponseEntity.ok("Manager registration request submitted successfully.");
    }

    @PostMapping("/verify-email")
    public ResponseEntity<LoginResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return ResponseEntity.ok(userService.verifyEmailAndLogin(request.getEmail(), request.getOtp()));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(@Valid @RequestBody ResendOtpRequest request) {
        userService.resendVerificationOtp(request.getEmail());
        return ResponseEntity.ok("If the registration is pending, a new OTP has been sent.");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.loginUser(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.forgotPassword(request.getEmail());
        return ResponseEntity.ok("If the account exists, a password reset OTP has been sent.");
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<String> verifyResetOtp(@Valid @RequestBody VerifyEmailRequest request) {
        userService.verifyResetOtp(request.getEmail(), request.getOtp());
        return ResponseEntity.ok("OTP verified successfully.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.ok("Password reset successfully.");
    }

    // Admin
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/managers")
    public ResponseEntity<List<UserResponse>> getApprovedManagers() {
        return ResponseEntity.ok(userService.getApprovedManagers());
    }

    @GetMapping("/manager-requests")
    public ResponseEntity<List<ManagerRegistrationResponse>> getManagerRegistrationRequests(
            @RequestParam(required = false) ManagerRegistrationStatus status) {
        return ResponseEntity.ok(userService.getManagerRegistrationRequests(status));
    }

    @PatchMapping("/manager-requests/{id}/approve")
    public ResponseEntity<ManagerRegistrationResponse> approveManagerRegistration(@PathVariable Long id) {
        return ResponseEntity.ok(userService.approveManagerRegistration(id));
    }

    @PatchMapping("/manager-requests/{id}/reject")
    public ResponseEntity<ManagerRegistrationResponse> rejectManagerRegistration(
            @PathVariable Long id,
            @RequestBody(required = false) ManagerRegistrationDecisionRequest request) {
        return ResponseEntity.ok(userService.rejectManagerRegistration(id, request));
    }

    // Profile
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    @PutMapping("/profile/change-password")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        userService.changePassword(userId, request);
        return ResponseEntity.ok("Password changed successfully.");
    }

    // Account Deactivation (self-service, authenticated)
    @PostMapping("/profile/deactivate/request")
    public ResponseEntity<String> requestDeactivation(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        userService.requestAccountDeactivation(userId);
        return ResponseEntity.ok("OTP sent to your registered email to confirm deactivation.");
    }

    @PostMapping("/profile/deactivate/confirm")
    public ResponseEntity<String> confirmDeactivation(
            @Valid @RequestBody DeactivateConfirmRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        userService.confirmAccountDeactivation(userId, request.getOtp());
        return ResponseEntity.ok("Account deactivated successfully.");
    }

    // Account Reactivation (public — user is blocked at login, can't send a JWT)
    @PostMapping("/reactivate/request")
    public ResponseEntity<String> requestReactivation(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.requestAccountReactivation(request.getEmail());
        return ResponseEntity.ok("If the account is deactivated, an OTP has been sent.");
    }

    @PostMapping("/reactivate/confirm")
    public ResponseEntity<LoginResponse> confirmReactivation(@Valid @RequestBody VerifyEmailRequest request) {
        return ResponseEntity.ok(userService.confirmAccountReactivation(request.getEmail(), request.getOtp()));
    }

    // Photo
    @PutMapping(value = "/profile/photo", consumes = "multipart/form-data")
    public ResponseEntity<UserResponse> updateProfilePhoto(
            @RequestParam("photo") MultipartFile file,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(userService.updateProfilePhoto(userId, file));
    }

    @GetMapping("/{id}/profile-photo")
    public ResponseEntity<byte[]> getProfilePhoto(@PathVariable Long id) {
        return userService.getProfilePhoto(id);
    }

    @DeleteMapping("/profile/photo")
    public ResponseEntity<Void> removeProfilePhoto(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        userService.removeProfilePhoto(userId);
        return ResponseEntity.noContent().build();
    }

    // Saved Addresses
    @GetMapping("/addresses")
    public ResponseEntity<List<SavedAddressResponse>> getMyAddresses(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(userService.getMyAddresses(userId));
    }

    @GetMapping("/addresses/default")
    public ResponseEntity<SavedAddressResponse> getDefaultAddress(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(userService.getDefaultAddress(userId));
    }

    @PostMapping("/addresses")
    public ResponseEntity<SavedAddressResponse> addAddress(
            @Valid @RequestBody SavedAddressRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(userService.addAddress(userId, request));
    }

    @PutMapping("/addresses/{id}")
    public ResponseEntity<SavedAddressResponse> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody SavedAddressRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(userService.updateAddress(userId, id, request));
    }

    @PatchMapping("/addresses/{id}/default")
    public ResponseEntity<SavedAddressResponse> setDefaultAddress(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(userService.setDefaultAddress(userId, id));
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        userService.deleteAddress(userId, id);
        return ResponseEntity.noContent().build();
    }
}