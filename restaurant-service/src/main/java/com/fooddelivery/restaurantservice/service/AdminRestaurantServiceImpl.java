package com.fooddelivery.restaurantservice.service;

import com.fooddelivery.restaurantservice.entity.MenuItem;
import com.fooddelivery.restaurantservice.entity.Restaurant;
import com.fooddelivery.restaurantservice.exception.DuplicateResourceException;
import com.fooddelivery.restaurantservice.exception.ResourceNotFoundException;
import com.fooddelivery.restaurantservice.repository.MenuItemRepository;
import com.fooddelivery.restaurantservice.repository.RestaurantRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminRestaurantServiceImpl implements AdminRestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public AdminRestaurantServiceImpl(RestaurantRepository restaurantRepository,
                                      MenuItemRepository menuItemRepository) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
    }


    @Override
    public Restaurant createRestaurant(Restaurant restaurant) {
        restaurantRepository
                .findByNameIgnoreCaseAndLocationIgnoreCase(
                        restaurant.getName(), restaurant.getLocation())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Restaurant with same name and location already exists");
                });

        return restaurantRepository.save(restaurant);
    }

    @Override
    public Restaurant updateRestaurant(Long id, Restaurant updated) {
        Restaurant existing = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        existing.setName(updated.getName());
        existing.setLocation(updated.getLocation());
        existing.setCuisine(updated.getCuisine());
        existing.setRating(updated.getRating());
        existing.setDeliveryTime(updated.getDeliveryTime());

        return restaurantRepository.save(existing);
    }

    @Override
    public void deleteRestaurant(Long id) {
        restaurantRepository.deleteById(id);
    }

    @Override
    public MenuItem addMenuItem(Long restaurantId, MenuItem menuItem) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        menuItem.setRestaurant(restaurant);
        return menuItemRepository.save(menuItem);
    }

    @Override
    public MenuItem updateMenuItem(Long itemId, MenuItem updated) {
        MenuItem existing = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setPrice(updated.getPrice());
        existing.setImageUrl(updated.getImageUrl());
        existing.setVeg(updated.getVeg());

        return menuItemRepository.save(existing);
    }

    @Override
    public void deleteMenuItem(Long itemId) {
        menuItemRepository.deleteById(itemId);
    }
}