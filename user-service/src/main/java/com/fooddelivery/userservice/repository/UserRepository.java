package com.fooddelivery.userservice.repository;

import com.fooddelivery.userservice.entity.User;
import com.fooddelivery.userservice.enums.AccountStatus;
import com.fooddelivery.userservice.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    List<User> findByRoleAndAccountStatusOrderByNameAsc(Role role, AccountStatus accountStatus);
}