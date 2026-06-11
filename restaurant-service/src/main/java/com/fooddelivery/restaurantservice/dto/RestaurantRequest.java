package com.fooddelivery.restaurantservice.dto;

import lombok.Data;

@Data
public class RestaurantRequest {
    private String name;
    private String location;
    private String cuisine;
    private Double rating;
    private Integer ratingCount;
    private Integer deliveryTime;
    private Boolean isActive = true;
}