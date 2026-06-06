package com.fooddelivery.orderservice.dto;

import com.fooddelivery.orderservice.enums.OrderStatus;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class UpdateOrderStatusRequest {
    private OrderStatus status;
}