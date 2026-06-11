package com.fooddelivery.restaurantservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRestaurantRatingRequest {
    private Double rating;
    private Integer ratingCount;
}