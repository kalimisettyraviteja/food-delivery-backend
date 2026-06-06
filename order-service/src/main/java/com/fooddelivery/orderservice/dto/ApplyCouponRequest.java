package com.fooddelivery.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;


@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ApplyCouponRequest {

    @NotBlank(message = "Coupon code is required")
    private String couponCode;

    @NotNull(message = "Restaurant id is required")
    private Long restaurantId;

    @NotNull(message = "Order total is required")
    @Positive(message = "Order total must be greater than zero")
    private Double orderAmount;
}