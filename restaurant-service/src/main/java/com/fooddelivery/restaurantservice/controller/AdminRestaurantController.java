package com.fooddelivery.restaurantservice.controller;

import com.fooddelivery.restaurantservice.dto.MenuItemRequest;
import com.fooddelivery.restaurantservice.dto.MenuItemResponse;
import com.fooddelivery.restaurantservice.dto.RestaurantRequest;
import com.fooddelivery.restaurantservice.dto.RestaurantResponse;
import com.fooddelivery.restaurantservice.service.AdminRestaurantService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/restaurants")
public class AdminRestaurantController {

    private final AdminRestaurantService adminRestaurantService;

    public AdminRestaurantController(AdminRestaurantService adminRestaurantService) {
        this.adminRestaurantService = adminRestaurantService;
    }

    @PostMapping
    public ResponseEntity<RestaurantResponse> createRestaurant(
            @Valid @RequestBody RestaurantRequest request) {
        return ResponseEntity.ok(adminRestaurantService.createRestaurant(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequest request) {
        return ResponseEntity.ok(adminRestaurantService.updateRestaurant(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id) {
        adminRestaurantService.deleteRestaurant(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> getAllRestaurants() {
        return ResponseEntity.ok(adminRestaurantService.getAllRestaurants());
    }

    @PatchMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RestaurantResponse> uploadRestaurantImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image) throws IOException {
        return ResponseEntity.ok(adminRestaurantService.updateRestaurantImage(id, image));
    }

    @DeleteMapping("/{id}/image")
    public ResponseEntity<Void> deleteRestaurantImage(@PathVariable Long id) {
        adminRestaurantService.deleteRestaurantImage(id);
        return ResponseEntity.noContent().build();
    }


    //------------------------------------------------------------------

    @PostMapping("/{restaurantId}/menu-items")
    public ResponseEntity<MenuItemResponse> addMenuItem(
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(adminRestaurantService.addMenuItem(restaurantId, request));
    }

    @PutMapping("/menu-items/{itemId}")
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @PathVariable Long itemId,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(adminRestaurantService.updateMenuItem(itemId, request));
    }

    @DeleteMapping("/menu-items/{itemId}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long itemId) {
        adminRestaurantService.deleteMenuItem(itemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{restaurantId}/menu-items")
    public ResponseEntity<List<MenuItemResponse>> getAllMenuItems(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(adminRestaurantService.getAllMenuItems(restaurantId));
    }

    @PatchMapping(value = "/menu-items/{itemId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MenuItemResponse> uploadMenuItemImage(
            @PathVariable Long itemId,
            @RequestParam("image") MultipartFile image) throws IOException {
        return ResponseEntity.ok(adminRestaurantService.updateMenuItemImage(itemId, image));
    }

    @DeleteMapping("/menu-items/{itemId}/image")
    public ResponseEntity<Void> deleteMenuItemImage(@PathVariable Long itemId) {
        adminRestaurantService.deleteMenuItemImage(itemId);
        return ResponseEntity.noContent().build();
    }
}