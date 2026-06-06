package com.fooddelivery.orderservice.entity;

import com.fooddelivery.orderservice.enums.CouponScope;
import com.fooddelivery.orderservice.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "coupons")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false)
    private Double discountValue;

    // For PERCENTAGE type — max discount cap
    private Double maxDiscountAmount;

    // Minimum order total required to use this coupon
    @Column(nullable = false)
    private Double minOrderAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponScope scope;

    // null if GLOBAL
    private Long restaurantId;

    @Column(nullable = false)
    private boolean active;

    private LocalDate expiryDate;
}