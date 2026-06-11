package com.fooddelivery.orderservice.service;

import com.fooddelivery.orderservice.dto.CreateReviewRequest;
import com.fooddelivery.orderservice.dto.RestaurantRatingSummaryResponse;
import com.fooddelivery.orderservice.dto.ReviewResponse;

import java.util.List;

public interface ReviewService {
    ReviewResponse createReview(Long userId, String userEmail, CreateReviewRequest request);
    ReviewResponse getReviewByOrderId(Long orderId, Long userId);
    List<ReviewResponse> getReviewsByRestaurantId(Long restaurantId);
    RestaurantRatingSummaryResponse getRestaurantRatingSummary(Long restaurantId);
}
