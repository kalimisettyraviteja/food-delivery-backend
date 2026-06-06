package com.fooddelivery.orderservice.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ApplyCouponResponse {
    private String couponCode;
    private String description;
    private Double originalAmount;
    private Double discountAmount;
    private Double deliveryCharge;
    private Double finalAmount;
    private String message;
}