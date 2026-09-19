package com.fooddelivery.userservice.repository;

import com.fooddelivery.userservice.entity.ManagerRegistrationRequest;
import com.fooddelivery.userservice.enums.ManagerRegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ManagerRegistrationRequestRepository extends JpaRepository<ManagerRegistrationRequest, Long> {

    Optional<ManagerRegistrationRequest> findByEmailIgnoreCase(String email);

    List<ManagerRegistrationRequest> findByStatusOrderByCreatedAtDesc(ManagerRegistrationStatus status);

    List<ManagerRegistrationRequest> findAllByOrderByCreatedAtDesc();
}