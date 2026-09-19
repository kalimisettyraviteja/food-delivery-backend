package com.fooddelivery.orderservice.service;

import com.fooddelivery.orderservice.client.RestaurantClient;
import com.fooddelivery.orderservice.dto.RestaurantRatingSummaryResponse;
import com.fooddelivery.orderservice.dto.ReviewResponse;
import com.fooddelivery.orderservice.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagerReviewServiceImpl implements ManagerReviewService {

    private final RestaurantClient restaurantClient;
    private final ReviewService reviewService;

    @Override
    public List<ReviewResponse> getReviewsForManager(
            Long managerId,
            Long restaurantId
    ) {
        validateManagerOwnsRestaurant(managerId, restaurantId);

        return reviewService.getReviewsByRestaurantId(restaurantId);
    }

    @Override
    public RestaurantRatingSummaryResponse getRatingSummaryForManager(
            Long managerId,
            Long restaurantId
    ) {
        validateManagerOwnsRestaurant(managerId, restaurantId);

        return reviewService.getRestaurantRatingSummary(restaurantId);
    }

    private void validateManagerOwnsRestaurant(
            Long managerId,
            Long restaurantId
    ) {
        List<Long> restaurantIds;

        try {
            restaurantIds =
                    restaurantClient.getRestaurantIdsByManagerId(managerId);
        } catch (Exception ex) {
            log.error(
                    "Failed to fetch owned restaurant IDs for managerId={}",
                    managerId,
                    ex
            );

            throw new IllegalStateException(
                    "Unable to verify restaurant ownership",
                    ex
            );
        }

        if (restaurantIds == null || !restaurantIds.contains(restaurantId)) {
            throw new ResourceNotFoundException(
                    "Restaurant not found or you do not have access to its reviews."
            );
        }
    }
}