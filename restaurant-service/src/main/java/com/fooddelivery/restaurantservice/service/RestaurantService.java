package com.fooddelivery.restaurantservice.service;

import com.fooddelivery.restaurantservice.entity.MenuItem;
import com.fooddelivery.restaurantservice.entity.Restaurant;

import java.util.List;

public interface RestaurantService {

    List<Restaurant> searchRestaurants(String location, String cuisine);

    List<MenuItem> getMenuForRestaurant(Long restaurantId, Boolean vegOnly);
}