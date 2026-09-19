package com.fooddelivery.orderservice.service;

import com.fooddelivery.orderservice.dto.RestaurantRatingSummaryResponse;
import com.fooddelivery.orderservice.dto.ReviewResponse;

import java.util.List;

public interface ManagerReviewService {

    List<ReviewResponse> getReviewsForManager(Long managerId, Long restaurantId);

    RestaurantRatingSummaryResponse getRatingSummaryForManager(Long managerId, Long restaurantId);
}