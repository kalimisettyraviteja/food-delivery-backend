package com.fooddelivery.orderservice.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OrderItemResponse {
    private Long menuItemId;
    private String itemName;
    private Double price;
    private Integer quantity;
    private Double subtotal;
}