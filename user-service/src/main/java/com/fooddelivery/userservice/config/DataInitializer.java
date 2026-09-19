package com.fooddelivery.userservice.config;

import com.fooddelivery.userservice.entity.User;
import com.fooddelivery.userservice.enums.AccountStatus;
import com.fooddelivery.userservice.enums.Role;
import com.fooddelivery.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmailIgnoreCase("admin@gmail.com")) {
            User admin = User.builder()
                    .name("Admin")
                    .email("admin@gmail.com")
                    .password(passwordEncoder.encode("123456"))
                    .phone("0000000000")
                    .role(Role.ADMIN)
                    .accountStatus(AccountStatus.ACTIVE)
                    .mustChangePassword(false)
                    .build();

            userRepository.save(admin);
            System.out.println("✅ Admin user created successfully → admin@gmail.com");
        } else {
            System.out.println("ℹ️ Admin user already exists → skipping creation");
        }
    }}
