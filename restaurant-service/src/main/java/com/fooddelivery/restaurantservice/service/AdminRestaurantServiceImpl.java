package com.fooddelivery.restaurantservice.service;

import com.fooddelivery.restaurantservice.dto.MenuItemRequest;
import com.fooddelivery.restaurantservice.dto.MenuItemResponse;
import com.fooddelivery.restaurantservice.dto.RestaurantRequest;
import com.fooddelivery.restaurantservice.dto.RestaurantResponse;
import com.fooddelivery.restaurantservice.entity.MenuItem;
import com.fooddelivery.restaurantservice.entity.Restaurant;
import com.fooddelivery.restaurantservice.exception.DuplicateResourceException;
import com.fooddelivery.restaurantservice.exception.ResourceNotFoundException;
import com.fooddelivery.restaurantservice.repository.MenuItemRepository;
import com.fooddelivery.restaurantservice.repository.RestaurantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public RestaurantResponse createRestaurant(RestaurantRequest request) {
        restaurantRepository
                .findByNameIgnoreCaseAndLocationIgnoreCase(request.getName(), request.getLocation())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Restaurant with same name and location already exists");
                });

        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .location(request.getLocation())
                .cuisine(request.getCuisine())
                .rating(request.getRating())
                .deliveryTime(request.getDeliveryTime())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        return mapToRestaurantResponse(restaurantRepository.save(restaurant));
    }


    @Override
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request) {
        Restaurant existing = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        existing.setName(request.getName());
        existing.setLocation(request.getLocation());
        existing.setCuisine(request.getCuisine());
        existing.setRating(request.getRating());
        existing.setDeliveryTime(request.getDeliveryTime());
        existing.setIsActive(request.getIsActive() != null ? request.getIsActive() : existing.getIsActive());

        return mapToRestaurantResponse(restaurantRepository.save(existing));
    }



    @Override
    public void deleteRestaurant(Long id) {
        restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        restaurantRepository.deleteById(id);
    }


    @Override
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll()
                .stream()
                .map(this::mapToRestaurantResponse)
                .toList();
    }


    @Override
    public MenuItemResponse addMenuItem(Long restaurantId, MenuItemRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        MenuItem menuItem = MenuItem.builder()
                .restaurant(restaurant)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .image(request.getImage())
                .veg(request.getVeg())
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .build();

        return mapToMenuItemResponse(menuItemRepository.save(menuItem));
    }



    @Override
    public MenuItemResponse updateMenuItem(Long itemId, MenuItemRequest request) {
        MenuItem existing = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setPrice(request.getPrice());
        existing.setImage(request.getImage());
        existing.setVeg(request.getVeg());
        existing.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : existing.getIsAvailable());

        return mapToMenuItemResponse(menuItemRepository.save(existing));
    }


    @Override
    public void deleteMenuItem(Long itemId) {
        menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        menuItemRepository.deleteById(itemId);
    }

    @Override
    public List<MenuItemResponse> getAllMenuItems(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        return menuItemRepository.findByRestaurant(restaurant)
                .stream()
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