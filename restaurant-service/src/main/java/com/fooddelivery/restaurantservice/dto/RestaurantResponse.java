package com.fooddelivery.restaurantservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantResponse {
    private Long id;
    private String name;
    private String location;
    private String cuisine;
    private Double rating;
    private Integer ratingCount;
    private Boolean isActive;
    private Boolean isPureVeg;
    private String image;
    private Double latitude;
    private Double longitude;
    private Long managerId;
    private Double distanceKm;
    private Integer estimatedMinutes;
}