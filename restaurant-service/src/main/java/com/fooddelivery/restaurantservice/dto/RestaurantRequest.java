package com.fooddelivery.restaurantservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RestaurantRequest {

    @NotBlank(message = "Restaurant name is required")
    private String name;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Cuisine type is required")
    private String cuisine;

    @DecimalMin(value = "0.0", message = "Rating cannot be negative")
    @DecimalMax(value = "5.0", message = "Rating cannot exceed 5.0")
    private Double rating;

    @Min(value = 0, message = "Rating count cannot be negative")
    private Integer ratingCount;

    @Min(value = 5, message = "Delivery time must be at least 5 minute's")
    private Integer deliveryTime;

    private Boolean isActive = true;
}