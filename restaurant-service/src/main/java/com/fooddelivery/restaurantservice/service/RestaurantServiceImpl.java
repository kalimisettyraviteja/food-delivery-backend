package com.fooddelivery.restaurantservice.service;

import com.fooddelivery.restaurantservice.entity.MenuItem;
import com.fooddelivery.restaurantservice.entity.Restaurant;
import com.fooddelivery.restaurantservice.repository.MenuItemRepository;
import com.fooddelivery.restaurantservice.repository.RestaurantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public RestaurantServiceImpl(RestaurantRepository restaurantRepository,
                                 MenuItemRepository menuItemRepository) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Override
    public List<Restaurant> searchRestaurants(String location, String cuisine) {
        String loc = (location == null || location.isBlank()) ? "" : location;
        String cui = (cuisine == null || cuisine.isBlank()) ? "" : cuisine;
        return restaurantRepository
                .findByLocationContainingIgnoreCaseAndCuisineContainingIgnoreCase(loc, cui);
    }

    @Override
    public List<MenuItem> getMenuForRestaurant(Long restaurantId, Boolean vegOnly) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        if (vegOnly == null) {
            return menuItemRepository.findByRestaurant(restaurant);
        }
        return menuItemRepository.findByRestaurantAndVeg(restaurant, vegOnly);
    }
}