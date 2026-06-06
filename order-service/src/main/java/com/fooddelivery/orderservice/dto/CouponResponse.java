package com.fooddelivery.orderservice.dto;


import com.fooddelivery.orderservice.enums.CouponScope;
import com.fooddelivery.orderservice.enums.DiscountType;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CouponResponse {
    private Long id;
    private String code;
    private String description;
    private DiscountType discountType;
    private Double discountValue;
    private Double maxDiscountAmount;
    private Double minOrderAmount;
    private CouponScope scope;
    private Long restaurantId;
    private boolean active;
    private LocalDate expiryDate;
}