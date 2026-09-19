package com.fooddelivery.userservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ManagerRegistrationResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String requestedRole;
    private String status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}