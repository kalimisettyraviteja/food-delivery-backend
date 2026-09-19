package com.fooddelivery.orderservice.client;

import com.fooddelivery.orderservice.dto.UpdateRestaurantRatingRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "restaurant-service")
public interface RestaurantClient {

    @PutMapping("/api/restaurants/internal/{restaurantId}/rating")
    void updateRestaurantRating(
            @PathVariable("restaurantId") Long restaurantId,
            @RequestBody UpdateRestaurantRatingRequest request
    );

    @GetMapping("/api/manager/internal/manager/{managerId}/restaurant-ids")
    List<Long> getRestaurantIdsByManagerId(
            @PathVariable("managerId") Long managerId
    );

}