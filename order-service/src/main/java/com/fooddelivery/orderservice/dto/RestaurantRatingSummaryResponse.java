package com.fooddelivery.orderservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantRatingSummaryResponse {
    private Long restaurantId;
    private Double averageRating;
    private Integer ratingCount;
}