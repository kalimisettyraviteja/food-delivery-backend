package com.fooddelivery.orderservice.dto;

import com.fooddelivery.orderservice.enums.CancelledBy;
import com.fooddelivery.orderservice.enums.OrderStatus;
import com.fooddelivery.orderservice.enums.PaymentMethod;
import com.fooddelivery.orderservice.enums.PaymentStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private Long restaurantId;
    private String restaurantName;
    private String restaurantLocation;
    private String restaurantCuisine;
    private Double restaurantLatitude;
    private Double restaurantLongitude;
    private Double restaurantDistanceKm;
    private Integer restaurantEstimatedMinutes;
    private Integer preparationBufferMinutes;
    private Integer finalEstimatedDeliveryMinutes;
    private LocalDateTime estimatedDeliveryAt;
    private Double originalAmount;
    private Double discountAmount;
    private Double deliveryCharge;
    private Double totalAmount;
    private String couponCode;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private OrderStatus status;
    private CancelledBy cancelledBy;
    private LocalDateTime cancelledAt;
    private Double refundAmount;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
    private OrderDeliveryAddressResponse deliveryAddress;
    private String cookingInstructions;
}