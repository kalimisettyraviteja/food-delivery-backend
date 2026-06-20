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
    private Integer deliveryTime;
    private Boolean isActive;
    private String image;
}