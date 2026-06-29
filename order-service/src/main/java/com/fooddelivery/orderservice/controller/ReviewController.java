package com.fooddelivery.orderservice.controller;

import com.fooddelivery.orderservice.dto.CreateReviewRequest;
import com.fooddelivery.orderservice.dto.RestaurantRatingSummaryResponse;
import com.fooddelivery.orderservice.dto.ReviewResponse;
import com.fooddelivery.orderservice.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            HttpServletRequest httpRequest) {

        Long userId = (Long) httpRequest.getAttribute("userId");
        String email = (String) httpRequest.getAttribute("email");

        return ResponseEntity.ok(reviewService.createReview(userId, email, request));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<?> getReviewByOrderId(
            @PathVariable Long orderId,
            HttpServletRequest httpRequest) {

        Long userId = (Long) httpRequest.getAttribute("userId");
        ReviewResponse review = reviewService.getReviewByOrderId(orderId, userId);

        if (review == null) {
            return ResponseEntity.status(404).body(
                    java.util.Map.of("message", "No review found for this order")
            );
        }

        return ResponseEntity.ok(review);
    }

    @GetMapping("/restaurants/{restaurantId}")
    public ResponseEntity<List<ReviewResponse>> getRestaurantReviews(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(reviewService.getReviewsByRestaurantId(restaurantId));
    }

    @GetMapping("/restaurants/{restaurantId}/summary")
    public ResponseEntity<RestaurantRatingSummaryResponse> getRestaurantRatingSummary(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(reviewService.getRestaurantRatingSummary(restaurantId));
    }
}