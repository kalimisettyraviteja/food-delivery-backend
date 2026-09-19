package com.fooddelivery.restaurantservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantSummaryResponse {
    private Long totalCount;
    private Long activeCount;
    private Long inactiveCount;
}