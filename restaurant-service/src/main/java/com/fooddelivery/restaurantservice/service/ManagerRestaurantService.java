package com.fooddelivery.restaurantservice.service;

import com.fooddelivery.restaurantservice.dto.MenuItemRequest;
import com.fooddelivery.restaurantservice.dto.MenuItemResponse;
import com.fooddelivery.restaurantservice.dto.RestaurantRequest;
import com.fooddelivery.restaurantservice.dto.RestaurantResponse;
import com.fooddelivery.restaurantservice.dto.RestaurantSummaryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ManagerRestaurantService {

    // Restaurants

    RestaurantResponse createRestaurant(Long managerId, RestaurantRequest request);

    RestaurantResponse updateRestaurant(Long managerId, Long restaurantId, RestaurantRequest request);

    void deleteRestaurant(Long managerId, Long restaurantId);

    RestaurantSummaryResponse getMyRestaurantSummary(Long managerId);

    List<RestaurantResponse> getMyRestaurants(Long managerId, String status);

    RestaurantResponse getMyRestaurantById(Long managerId, Long restaurantId);

    RestaurantResponse updateRestaurantImage(Long managerId, Long restaurantId, MultipartFile image) throws IOException;

    void deleteRestaurantImage(Long managerId, Long restaurantId);

    // Menu items

    MenuItemResponse addMenuItem(Long managerId, Long restaurantId, MenuItemRequest request);

    MenuItemResponse updateMenuItem(Long managerId, Long itemId, MenuItemRequest request);

    void deleteMenuItem(Long managerId, Long itemId);

    List<MenuItemResponse> getAllMenuItems(Long managerId, Long restaurantId);

    MenuItemResponse updateMenuItemImage(Long managerId, Long itemId, MultipartFile image) throws IOException;

    void deleteMenuItemImage(Long managerId, Long itemId);

    List<Long> getRestaurantIdsByManagerId(Long managerId);
}