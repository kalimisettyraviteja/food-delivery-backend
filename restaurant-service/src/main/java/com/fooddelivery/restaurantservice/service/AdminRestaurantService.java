package com.fooddelivery.restaurantservice.service;

import com.fooddelivery.restaurantservice.entity.MenuItem;
import com.fooddelivery.restaurantservice.entity.Restaurant;

public interface AdminRestaurantService {

    Restaurant createRestaurant(Restaurant restaurant);

    Restaurant updateRestaurant(Long id, Restaurant restaurant);

    void deleteRestaurant(Long id);

    MenuItem addMenuItem(Long restaurantId, MenuItem menuItem);

    MenuItem updateMenuItem(Long itemId, MenuItem menuItem);

    void deleteMenuItem(Long itemId);
}