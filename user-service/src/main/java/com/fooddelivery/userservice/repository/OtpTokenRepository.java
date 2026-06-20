package com.fooddelivery.userservice.repository;

import com.fooddelivery.userservice.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findByEmailAndOtpAndTypeAndUsedFalse(String email, String otp, String type);

    void deleteByEmailAndType(String email, String type);
}