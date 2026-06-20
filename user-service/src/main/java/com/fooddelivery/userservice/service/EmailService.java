package com.fooddelivery.userservice.service;

public interface EmailService {

    void sendVerificationOtp(String toEmail, String name, String otp);

    void sendPasswordResetOtp(String toEmail, String name, String otp);
}