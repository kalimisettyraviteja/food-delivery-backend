package com.fooddelivery.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeactivateConfirmRequest {

    @NotBlank(message = "OTP is required")
    private String otp;
}