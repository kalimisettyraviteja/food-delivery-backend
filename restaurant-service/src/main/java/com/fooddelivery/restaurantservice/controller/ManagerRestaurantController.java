package com.fooddelivery.restaurantservice.controller;

import com.fooddelivery.restaurantservice.dto.MenuItemRequest;
import com.fooddelivery.restaurantservice.dto.MenuItemResponse;
import com.fooddelivery.restaurantservice.dto.RestaurantRequest;
import com.fooddelivery.restaurantservice.dto.RestaurantResponse;
import com.fooddelivery.restaurantservice.dto.RestaurantSummaryResponse;
import com.fooddelivery.restaurantservice.service.ManagerRestaurantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/manager")
public class ManagerRestaurantController {

    private final ManagerRestaurantService managerRestaurantService;

    public ManagerRestaurantController(ManagerRestaurantService managerRestaurantService) {
        this.managerRestaurantService = managerRestaurantService;
    }

    // ─────────────────────────────────────────────
    // Restaurants (manager-owned)
    // ─────────────────────────────────────────────

    @PostMapping("/restaurants")
    public ResponseEntity<RestaurantResponse> createRestaurant(
            @Valid @RequestBody RestaurantRequest request,
            HttpServletRequest httpRequest
    ) {
        Long managerId = (Long) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(
                managerRestaurantService.createRestaurant(managerId, request)
        );
    }

    @PutMapping("/restaurants/{id}")
    public ResponseEntity<RestaurantResponse> updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequest request,
            HttpServletRequest httpRequest
    ) {
        Long managerId = (Long) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(
                managerRestaurantService.updateRestaurant(managerId, id, request)
        );
    }

    @DeleteMapping("/restaurants/{id}")
    public ResponseEntity<Void> deleteRestaurant(
            @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        Long managerId = (Long) httpRequest.getAttribute("userId");
        managerRestaurantService.deleteRestaurant(managerId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Summary counts for dashboard cards (total/active/inactive).
     */
    @GetMapping("/restaurants/summary")
    public ResponseEntity<RestaurantSummaryResponse> getMyRestaurantSummary(
            HttpServletRequest httpRequest
    ) {
        Long managerId = (Long) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(
                managerRestaurantService.getMyRestaurantSummary(managerId)
        );
    }

    /**
     * List manager-owned restaurants, optionally filtered by status.
     * status = null or empty → all
     * status = ACTIVE        → only active
     * status = INACTIVE      → only inactive
     */
    @GetMapping("/restaurants")
    public ResponseEntity<List<RestaurantResponse>> getMyRestaurants(
            @RequestParam(required = false) String status,
            HttpServletRequest httpRequest
    ) {
        Long managerId = (Long) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(
                managerRestaurantService.getMyRestaurants(managerId, status)
        );
    }

    @GetMapping("/restaurants/{id}")
    public ResponseEntity<RestaurantResponse> getMyRestaurantById(
            @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        Long managerId = (Long) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(
                managerRestaurantService.getMyRestaurantById(managerId, id)
        );
    }

    @PatchMapping(
            value = "/restaurants/{id}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<RestaurantResponse> uploadRestaurantImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image,
            HttpServletRequest httpRequest
    ) throws IOException {
        Long managerId = (Long) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(
                managerRestaurantService.updateRestaurantImage(managerId, id, image)
        );
    }

    @DeleteMapping("/restaurants/{id}/image")
    public ResponseEntity<Void> deleteRestaurantImage(
            @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        Long managerId = (Long) httpRequest.getAttribute("userId");
        managerRestaurantService.deleteRestaurantImage(managerId, id);
        return ResponseEntity.noContent().build();
    }

    // ─────────────────────────────────────────────
    // Menu items (manager-owned restaurants)
    // ─────────────────────────────────────────────

    @PostMapping("/restaurants/{restaurantId}/menu-items")
    public ResponseEntity<MenuItemResponse> addMenuItem(
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuItemRequest request,
            HttpServletRequest httpRequest
    ) {
        Long managerId = (Long) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(
                managerRestaurantService.addMenuItem(managerId, restaurantId, request)
        );
    }

    @PutMapping("/menu-items/{itemId}")
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @PathVariable Long itemId,
            @Valid @RequestBody MenuItemRequest request,
            HttpServletRequest httpRequest
    ) {
        Long managerId = (Long) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(
                managerRestaurantService.updateMenuItem(managerId, itemId, request)
        );
    }

    @DeleteMapping("/menu-items/{itemId}")
    public ResponseEntity<Void> deleteMenuItem(
            @PathVariable Long itemId,
            HttpServletRequest httpRequest
    ) {
        Long managerId = (Long) httpRequest.getAttribute("userId");
        managerRestaurantService.deleteMenuItem(managerId, itemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/restaurants/{restaurantId}/menu-items")
    public ResponseEntity<List<MenuItemResponse>> getAllMenuItems(
            @PathVariable Long restaurantId,
            HttpServletRequest httpRequest
    ) {
        Long managerId = (Long) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(
                managerRestaurantService.getAllMenuItems(managerId, restaurantId)
        );
    }

    @PatchMapping(
            value = "/menu-items/{itemId}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<MenuItemResponse> uploadMenuItemImage(
            @PathVariable Long itemId,
            @RequestParam("image") MultipartFile image,
            HttpServletRequest httpRequest
    ) throws IOException {
        Long managerId = (Long) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(
                managerRestaurantService.updateMenuItemImage(managerId, itemId, image)
        );
    }

    @DeleteMapping("/menu-items/{itemId}/image")
    public ResponseEntity<Void> deleteMenuItemImage(
            @PathVariable Long itemId,
            HttpServletRequest httpRequest
    ) {
        Long managerId = (Long) httpRequest.getAttribute("userId");
        managerRestaurantService.deleteMenuItemImage(managerId, itemId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Internal endpoint: returns IDs of restaurants owned by a given manager.
     * Used by other services (e.g. order-service) to enforce manager-level authorization.
     */
    @GetMapping("/internal/manager/{managerId}/restaurant-ids")
    public ResponseEntity<List<Long>> getRestaurantIdsByManagerId(
            @PathVariable Long managerId
    ) {
        List<Long> ids = managerRestaurantService.getRestaurantIdsByManagerId(managerId);
        return ResponseEntity.ok(ids);
    }

}