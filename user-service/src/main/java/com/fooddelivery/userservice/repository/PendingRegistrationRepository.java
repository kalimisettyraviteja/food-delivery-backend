package com.fooddelivery.userservice.repository;

import com.fooddelivery.userservice.entity.PendingRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, Long> {

    Optional<PendingRegistration> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Modifying
    @Transactional
    @Query("DELETE FROM PendingRegistration p WHERE LOWER(p.email) = LOWER(:email)")
    void deleteByEmail(@Param("email") String email);
}