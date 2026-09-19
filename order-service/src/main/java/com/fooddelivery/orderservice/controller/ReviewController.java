package com.fooddelivery.orderservice.controller;

import com.fooddelivery.orderservice.dto.CreateReviewRequest;
import com.fooddelivery.orderservice.dto.ReviewResponse;
import com.fooddelivery.orderservice.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = getUserId(httpRequest);
        String email = (String) httpRequest.getAttribute("email");

        return ResponseEntity.ok(
                reviewService.createReview(userId, email, request)
        );
    }

    /**
     * Customer retrieves only the review linked to their own order.
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ReviewResponse> getReviewByOrderId(
            @PathVariable Long orderId,
            HttpServletRequest httpRequest
    ) {
        Long userId = getUserId(httpRequest);

        return ResponseEntity.ok(
                reviewService.getReviewByOrderId(orderId, userId)
        );
    }

    private Long getUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");

        if (userId == null) {
            throw new RuntimeException("Authenticated user ID is missing");
        }

        if (userId instanceof Long id) {
            return id;
        }

        return Long.valueOf(userId.toString());
    }
}