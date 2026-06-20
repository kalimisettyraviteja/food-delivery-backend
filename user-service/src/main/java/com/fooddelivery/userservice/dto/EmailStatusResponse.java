package com.fooddelivery.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailStatusResponse {
    private String message;
    private String nextStep; // LOGIN, REGISTER, PENDING_VERIFICATION
    private String email;
    private String name;
}