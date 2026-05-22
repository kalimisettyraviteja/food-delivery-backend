package com.fooddelivery.restaurantservice.repository;

import com.fooddelivery.restaurantservice.entity.MenuItem;
import com.fooddelivery.restaurantservice.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByRestaurant(Restaurant restaurant);

    List<MenuItem> findByRestaurantAndVeg(Restaurant restaurant, Boolean veg);
}