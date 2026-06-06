package com.fooddelivery.orderservice.dto;


import com.fooddelivery.orderservice.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class PlaceOrderRequest {

    @NotNull(message = "Restaurant id is required")
    private Long restaurantId;

    @NotBlank(message = "Restaurant name is required")
    private String restaurantName;

    private String couponCode;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotEmpty(message = "Order items are required")
    private List<@Valid OrderItemRequest> items;
}