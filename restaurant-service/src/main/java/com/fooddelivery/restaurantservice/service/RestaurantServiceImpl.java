package com.fooddelivery.restaurantservice.service;

import com.fooddelivery.restaurantservice.dto.MenuItemResponse;
import com.fooddelivery.restaurantservice.dto.RestaurantResponse;
import com.fooddelivery.restaurantservice.entity.MenuItem;
import com.fooddelivery.restaurantservice.entity.Restaurant;
import com.fooddelivery.restaurantservice.exception.ResourceNotFoundException;
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
    public List<RestaurantResponse> searchRestaurants(String location, String cuisine) {
        String loc = (location == null || location.isBlank()) ? "" : location;
        String cui = (cuisine == null || cuisine.isBlank()) ? "" : cuisine;

        return restaurantRepository
                .findByLocationContainingIgnoreCaseAndCuisineContainingIgnoreCase(loc, cui)
                .stream()
                .map(this::mapToRestaurantResponse)
                .toList();
    }


    @Override
    public List<MenuItemResponse> getAvailableMenuForRestaurant(Long restaurantId, Boolean vegOnly) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        List<MenuItem> items;

        if (vegOnly == null) {
            items = menuItemRepository.findByRestaurantAndIsAvailableTrue(restaurant);
        } else {
            items = menuItemRepository.findByRestaurantAndIsAvailableTrueAndVeg(restaurant, vegOnly);
        }

        return items.stream()
                .map(this::mapToMenuItemResponse)
                .toList();
    }

    private RestaurantResponse mapToRestaurantResponse(Restaurant r) {
        return RestaurantResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .location(r.getLocation())
                .cuisine(r.getCuisine())
                .rating(r.getRating())
                .deliveryTime(r.getDeliveryTime())
                .isActive(r.getIsActive())
                .build();
    }

    private MenuItemResponse mapToMenuItemResponse(MenuItem m) {
        return MenuItemResponse.builder()
                .id(m.getId())
                .restaurantId(m.getRestaurant().getId())
                .name(m.getName())
                .description(m.getDescription())
                .price(m.getPrice())
                .image(m.getImage())
                .veg(m.getVeg())
                .isAvailable(m.getIsAvailable())
                .build();
    }

}