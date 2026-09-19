package com.fooddelivery.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderInstructionsRequest {

    @NotBlank(message = "Cooking Instructions cannot be empty")
    @Size(max = 500, message = "Cooking instructions must be under 500 characters")
    private String cookingInstructions;
}