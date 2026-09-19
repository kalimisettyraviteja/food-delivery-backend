package com.fooddelivery.orderservice.controller;

import com.fooddelivery.orderservice.dto.RestaurantRatingSummaryResponse;
import com.fooddelivery.orderservice.dto.ReviewResponse;
import com.fooddelivery.orderservice.service.ManagerReviewService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/manager/reviews")
@RequiredArgsConstructor
public class ManagerReviewController {

    private final ManagerReviewService managerReviewService;

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getMyRestaurantReviews(
            @RequestParam Long restaurantId,
            HttpServletRequest request
    ) {
        Long managerId = getManagerId(request);

        return ResponseEntity.ok(
                managerReviewService.getReviewsForManager(
                        managerId,
                        restaurantId
                )
        );
    }

    @GetMapping("/summary")
    public ResponseEntity<RestaurantRatingSummaryResponse> getMyRestaurantRatingSummary(
            @RequestParam Long restaurantId,
            HttpServletRequest request
    ) {
        Long managerId = getManagerId(request);

        return ResponseEntity.ok(
                managerReviewService.getRatingSummaryForManager(
                        managerId,
                        restaurantId
                )
        );
    }

    private Long getManagerId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");

        if (userId == null) {
            throw new RuntimeException("Authenticated manager ID is missing");
        }

        if (userId instanceof Long managerId) {
            return managerId;
        }

        return Long.valueOf(userId.toString());
    }
}