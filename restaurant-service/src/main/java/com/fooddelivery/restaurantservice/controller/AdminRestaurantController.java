package com.fooddelivery.restaurantservice.controller;

import com.fooddelivery.restaurantservice.entity.MenuItem;
import com.fooddelivery.restaurantservice.entity.Restaurant;
import com.fooddelivery.restaurantservice.service.AdminRestaurantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/restaurants")
public class AdminRestaurantController {

    private final AdminRestaurantService adminRestaurantService;

    public AdminRestaurantController(AdminRestaurantService adminRestaurantService) {
        this.adminRestaurantService = adminRestaurantService;
    }

    @PostMapping
    public ResponseEntity<Restaurant> createRestaurant(@RequestBody Restaurant restaurant) {
        return ResponseEntity.ok(adminRestaurantService.createRestaurant(restaurant));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Restaurant> updateRestaurant(
            @PathVariable Long id,
            @RequestBody Restaurant restaurant) {
        return ResponseEntity.ok(adminRestaurantService.updateRestaurant(id, restaurant));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id) {
        adminRestaurantService.deleteRestaurant(id);
        return ResponseEntity.noContent().build();
    }



    @PostMapping("/{restaurantId}/menu-items")
    public ResponseEntity<MenuItem> addMenuItem(
            @PathVariable Long restaurantId,
            @RequestBody MenuItem menuItem) {
        return ResponseEntity.ok(adminRestaurantService.addMenuItem(restaurantId, menuItem));
    }

    @PutMapping("/menu-items/{itemId}")
    public ResponseEntity<MenuItem> updateMenuItem(
            @PathVariable Long itemId,
            @RequestBody MenuItem menuItem) {
        return ResponseEntity.ok(adminRestaurantService.updateMenuItem(itemId, menuItem));
    }

    @DeleteMapping("/menu-items/{itemId}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long itemId) {
        adminRestaurantService.deleteMenuItem(itemId);
        return ResponseEntity.noContent().build();
    }


}