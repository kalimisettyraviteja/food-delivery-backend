package com.fooddelivery.restaurantservice.service;

import com.fooddelivery.restaurantservice.dto.MenuItemRequest;
import com.fooddelivery.restaurantservice.dto.MenuItemResponse;
import com.fooddelivery.restaurantservice.dto.RestaurantRequest;
import com.fooddelivery.restaurantservice.dto.RestaurantResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AdminRestaurantService {

    RestaurantResponse createRestaurant(RestaurantRequest request);
    RestaurantResponse updateRestaurant(Long id, RestaurantRequest request);
    void deleteRestaurant(Long id);
    List<RestaurantResponse> getAllRestaurants();
    RestaurantResponse updateRestaurantImage(Long id, MultipartFile image) throws IOException;
    void deleteRestaurantImage(Long id);

    MenuItemResponse addMenuItem(Long restaurantId, MenuItemRequest request);
    MenuItemResponse updateMenuItem(Long itemId, MenuItemRequest request);
    void deleteMenuItem(Long itemId);
    List<MenuItemResponse> getAllMenuItems(Long restaurantId);
    MenuItemResponse updateMenuItemImage(Long itemId, MultipartFile image) throws IOException;
    void deleteMenuItemImage(Long itemId);

}