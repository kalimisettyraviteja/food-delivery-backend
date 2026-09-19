package com.fooddelivery.userservice.repository;

import com.fooddelivery.userservice.entity.SavedAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedAddressRepository extends JpaRepository<SavedAddress, Long> {

    List<SavedAddress> findByUserIdOrderByIsDefaultDescIdDesc(Long userId);

    Optional<SavedAddress> findByIdAndUserId(Long id, Long userId);

    Optional<SavedAddress> findByUserIdAndIsDefaultTrue(Long userId);

    long countByUserId(Long userId);

    List<SavedAddress> findByUserId(Long userId);
}