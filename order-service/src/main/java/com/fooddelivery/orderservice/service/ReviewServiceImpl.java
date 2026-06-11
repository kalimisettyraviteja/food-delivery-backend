package com.fooddelivery.orderservice.service;

import com.fooddelivery.orderservice.client.RestaurantClient;
import com.fooddelivery.orderservice.dto.CreateReviewRequest;
import com.fooddelivery.orderservice.dto.RestaurantRatingSummaryResponse;
import com.fooddelivery.orderservice.dto.ReviewResponse;
import com.fooddelivery.orderservice.dto.UpdateRestaurantRatingRequest;
import com.fooddelivery.orderservice.entity.Order;
import com.fooddelivery.orderservice.entity.Review;
import com.fooddelivery.orderservice.enums.OrderStatus;
import com.fooddelivery.orderservice.repository.OrderRepository;
import com.fooddelivery.orderservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final RestaurantClient restaurantClient;

    @Override
    public ReviewResponse createReview(Long userId, String userEmail, CreateReviewRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new RuntimeException("You can review only delivered orders");
        }

        if (reviewRepository.existsByOrderId(order.getId())) {
            throw new RuntimeException("Review already submitted for this order");
        }

        Review review = Review.builder()
                .orderId(order.getId())
                .restaurantId(order.getRestaurantId())
                .userId(userId)
                .userEmail(userEmail)
                .rating(request.getRating())
                .reviewText(request.getReviewText())
                .build();

        Review saved = reviewRepository.save(review);

        syncRestaurantRating(order.getRestaurantId());

        return map(saved);
    }

    @Override
    public ReviewResponse getReviewByOrderId(Long orderId, Long userId) {
        Review review = reviewRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        return map(review);
    }

    @Override
    public List<ReviewResponse> getReviewsByRestaurantId(Long restaurantId) {
        return reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public RestaurantRatingSummaryResponse getRestaurantRatingSummary(Long restaurantId) {
        Double avg = reviewRepository.findAverageRatingByRestaurantId(restaurantId);
        Long count = reviewRepository.countByRestaurantId(restaurantId);

        return RestaurantRatingSummaryResponse.builder()
                .restaurantId(restaurantId)
                .averageRating(avg == null ? 0.0 : Math.round(avg * 10.0) / 10.0)
                .ratingCount(count.intValue())
                .build();
    }

    private void syncRestaurantRating(Long restaurantId) {
        RestaurantRatingSummaryResponse summary = getRestaurantRatingSummary(restaurantId);

        restaurantClient.updateRestaurantRating(
                restaurantId,
                UpdateRestaurantRatingRequest.builder()
                        .rating(summary.getAverageRating())
                        .ratingCount(summary.getRatingCount())
                        .build()
        );
    }

    private ReviewResponse map(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .orderId(review.getOrderId())
                .restaurantId(review.getRestaurantId())
                .userId(review.getUserId())
                .userEmail(review.getUserEmail())
                .rating(review.getRating())
                .reviewText(review.getReviewText())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}