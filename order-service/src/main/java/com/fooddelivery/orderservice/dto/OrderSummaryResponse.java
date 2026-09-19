package com.fooddelivery.orderservice.dto;

import com.fooddelivery.orderservice.enums.OrderStatus;
import com.fooddelivery.orderservice.enums.PaymentMethod;
import com.fooddelivery.orderservice.enums.PaymentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSummaryResponse {
    private Long id;
    private String restaurantName;
    private String restaurantLocation;
    private Double restaurantDistanceKm;
    private Integer restaurantEstimatedMinutes;
    private Integer preparationBufferMinutes;
    private Integer finalEstimatedDeliveryMinutes;
    private LocalDateTime estimatedDeliveryAt;
    private Double totalAmount;
    private String couponCode;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private OrderStatus status;
    private LocalDateTime createdAt;
}