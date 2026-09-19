package com.fooddelivery.userservice.service;

public interface EmailService {

    void sendVerificationOtp(String toEmail, String name, String otp);

    void sendPasswordResetOtp(String toEmail, String name, String otp);

    void sendManagerApprovalCredentials(String toEmail, String name, String temporaryPassword);

    void sendDeactivationOtp(String toEmail, String name, String otp);

    void sendReactivationOtp(String toEmail, String name, String otp);
}