package com.fooddelivery.restaurantservice.controller;

import com.fooddelivery.restaurantservice.dto.MenuItemResponse;
import com.fooddelivery.restaurantservice.dto.RestaurantResponse;
import com.fooddelivery.restaurantservice.dto.UpdateRestaurantRatingRequest;
import com.fooddelivery.restaurantservice.service.RestaurantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> searchRestaurants(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String cuisine) {
        return ResponseEntity.ok(
                restaurantService.searchRestaurants(location, cuisine));
    }

    @GetMapping("/{restaurantId}/menu")
    public ResponseEntity<List<MenuItemResponse>> getMenu(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) Boolean veg) {
        return ResponseEntity.ok(
                restaurantService.getAvailableMenuForRestaurant(restaurantId, veg));
    }

    @PutMapping("/internal/{restaurantId}/rating")
    public ResponseEntity<Void> updateRestaurantRating(
            @PathVariable Long restaurantId,
            @RequestBody UpdateRestaurantRatingRequest request) {
        restaurantService.updateRestaurantRating(
                restaurantId,
                request.getRating(),
                request.getRatingCount()
        );
        return ResponseEntity.ok().build();
    }
}