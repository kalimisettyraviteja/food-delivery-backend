package com.fooddelivery.restaurantservice.dto;

import lombok.Data;

@Data
public class RestaurantSearchRequest {

    private String location;
    private String cuisine;
    private Boolean pureVegOnly;

    private Double lat;
    private Double lng;
}