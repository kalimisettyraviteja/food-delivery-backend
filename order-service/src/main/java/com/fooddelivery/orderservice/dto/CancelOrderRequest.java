package com.fooddelivery.orderservice.dto;

import com.fooddelivery.orderservice.enums.CancelledBy;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelOrderRequest {

    @NotNull(message = "cancelledBy is required")
    private CancelledBy cancelledBy;
}