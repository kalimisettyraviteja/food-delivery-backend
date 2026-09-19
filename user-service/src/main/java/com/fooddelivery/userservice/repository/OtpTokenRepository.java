package com.fooddelivery.userservice.repository;

import com.fooddelivery.userservice.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    @Query("SELECT o FROM OtpToken o WHERE LOWER(o.email) = LOWER(:email) AND o.otp = :otp AND o.type = :type AND o.used = false")
    Optional<OtpToken> findByEmailAndOtpAndTypeAndUsedFalse(@Param("email") String email,
                                                            @Param("otp") String otp,
                                                            @Param("type") String type);

    @Modifying
    @Transactional
    @Query("DELETE FROM OtpToken o WHERE LOWER(o.email) = LOWER(:email) AND o.type = :type")
    void deleteByEmailAndType(@Param("email") String email, @Param("type") String type);
}