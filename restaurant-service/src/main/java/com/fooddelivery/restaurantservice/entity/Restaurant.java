package com.fooddelivery.restaurantservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "restaurants", indexes = {
        @Index(name = "idx_restaurant_lat", columnList = "latitude"),
        @Index(name = "idx_restaurant_lng", columnList = "longitude"),
        @Index(name = "idx_restaurant_manager", columnList = "manager_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String cuisine;

    private Double rating;

    private Integer ratingCount;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean isPureVeg = false;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "manager_id")
    private Long managerId;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] image;
}