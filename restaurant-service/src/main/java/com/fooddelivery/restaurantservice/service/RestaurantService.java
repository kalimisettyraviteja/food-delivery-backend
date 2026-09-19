package com.fooddelivery.restaurantservice.service;

import com.fooddelivery.restaurantservice.dto.MenuItemResponse;
import com.fooddelivery.restaurantservice.dto.RestaurantResponse;

import java.util.List;

public interface RestaurantService {

    List<RestaurantResponse> searchRestaurants(String location, String cuisine, Double lat, Double lng, Boolean pureVegOnly);

    RestaurantResponse getRestaurantById(Long restaurantId);

    List<MenuItemResponse> getAvailableMenuForRestaurant(Long restaurantId, Boolean vegOnly);

    void updateRestaurantRating(Long restaurantId, Double rating, Integer ratingCount);
}