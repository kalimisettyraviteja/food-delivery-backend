package com.fooddelivery.orderservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRestaurantSnapshotRequest {
    private String location;
    private String cuisine;
    private String imageUrl;
    private Double latitude;
    private Double longitude;
    private Double distanceKm;
    private Integer estimatedMinutes;
}