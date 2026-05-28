package com.fooddelivery.restaurantservice.dto;

import lombok.Data;

@Data
public class MenuItemRequest {
    private String name;
    private String description;
    private Double price;
    private String image;
    private Boolean veg;
    private Boolean isAvailable = true;
}