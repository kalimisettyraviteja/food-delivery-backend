package com.fooddelivery.orderservice.entity;

import com.fooddelivery.orderservice.enums.CancelledBy;
import com.fooddelivery.orderservice.enums.OrderStatus;
import com.fooddelivery.orderservice.enums.PaymentMethod;
import com.fooddelivery.orderservice.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private Long restaurantId;

    @Column(nullable = false)
    private String restaurantName;

    @Column(name = "restaurant_location")
    private String restaurantLocation;

    @Column(name = "restaurant_cuisine")
    private String restaurantCuisine;

    @Column(name = "restaurant_latitude")
    private Double restaurantLatitude;

    @Column(name = "restaurant_longitude")
    private Double restaurantLongitude;

    @Column(name = "restaurant_distance_km")
    private Double restaurantDistanceKm;

    @Column(name = "restaurant_estimated_minutes")
    private Integer restaurantEstimatedMinutes;

    @Column(name = "preparation_buffer_minutes")
    private Integer preparationBufferMinutes;

    @Column(name = "final_estimated_delivery_minutes")
    private Integer finalEstimatedDeliveryMinutes;

    @Column(name = "estimated_delivery_at")
    private LocalDateTime estimatedDeliveryAt;

    @Column(nullable = false)
    private Double originalAmount;

    @Column(nullable = false)
    private Double discountAmount;

    @Column(nullable = false)
    private Double deliveryCharge;

    @Column(nullable = false)
    private Double totalAmount;

    private String couponCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    // ─── NEW: cancellation audit fields ───
    @Enumerated(EnumType.STRING)
    @Column(name = "cancelled_by")
    private CancelledBy cancelledBy;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "refund_amount")
    private Double refundAmount;

    @Embedded
    private OrderDeliveryAddress deliveryAddress;

    @Column(name = "cooking_instructions", length = 500)
    private String cookingInstructions;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}