package com.fooddelivery.restaurantservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class MenuItemRequest {

    @NotBlank(message = "Menu item name is required")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private Double price;

    @NotNull(message = "Veg/Non-veg flag is required")
    private Boolean veg;

    private Boolean isAvailable = true;
}