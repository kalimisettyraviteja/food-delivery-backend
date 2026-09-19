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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
public class AdminRestaurantServiceImpl implements AdminRestaurantService {

    private static final List<String> ALLOWED_CONTENT_TYPES =
            List.of("image/jpeg", "image/png", "image/jpg", "image/webp");

    private static final double DUPLICATE_BRANCH_RADIUS_KM = 0.5;

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public AdminRestaurantServiceImpl(RestaurantRepository restaurantRepository,
                                      MenuItemRepository menuItemRepository) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Override
    public RestaurantResponse createRestaurant(RestaurantRequest request) {

        validateNoDuplicateBranch(request.getName(), request.getLatitude(), request.getLongitude(), null);

        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .location(request.getLocation())
                .cuisine(request.getCuisine())
                .rating(request.getRating())
                .ratingCount(request.getRatingCount())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .isPureVeg(request.getIsPureVeg() != null ? request.getIsPureVeg() : false)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .managerId(request.getManagerId())
                .build();

        return mapToRestaurantResponse(restaurantRepository.save(restaurant));
    }

    @Override
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request) {
        Restaurant existing = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        validateNoDuplicateBranch(request.getName(), request.getLatitude(), request.getLongitude(), id);

        existing.setName(request.getName());
        existing.setLocation(request.getLocation());
        existing.setCuisine(request.getCuisine());
        existing.setRating(request.getRating());
        existing.setRatingCount(request.getRatingCount());
        existing.setIsActive(request.getIsActive() != null ? request.getIsActive() : existing.getIsActive());
        existing.setIsPureVeg(request.getIsPureVeg() != null ? request.getIsPureVeg() : existing.getIsPureVeg());
        existing.setLatitude(request.getLatitude() != null ? request.getLatitude() : existing.getLatitude());
        existing.setLongitude(request.getLongitude() != null ? request.getLongitude() : existing.getLongitude());
        existing.setManagerId(request.getManagerId());

        return mapToRestaurantResponse(restaurantRepository.save(existing));
    }

    @Override
    public RestaurantResponse updateRestaurantImage(Long id, MultipartFile image) throws IOException {
        validateImageFile(image);

        Restaurant existing = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        existing.setImage(image.getBytes());
        return mapToRestaurantResponse(restaurantRepository.save(existing));
    }

    @Override
    public void deleteRestaurantImage(Long id) {
        Restaurant existing = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        existing.setImage(null);
        restaurantRepository.save(existing);
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

        if (Boolean.TRUE.equals(restaurant.getIsPureVeg()) && Boolean.FALSE.equals(request.getVeg())) {
            throw new IllegalArgumentException(
                    "This restaurant is marked Pure Veg. Non-veg menu items are not allowed."
            );
        }

        MenuItem menuItem = MenuItem.builder()
                .restaurant(restaurant)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .veg(request.getVeg())
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .build();

        return mapToMenuItemResponse(menuItemRepository.save(menuItem));
    }

    @Override
    public MenuItemResponse updateMenuItem(Long itemId, MenuItemRequest request) {
        MenuItem existing = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        if (Boolean.TRUE.equals(existing.getRestaurant().getIsPureVeg()) && Boolean.FALSE.equals(request.getVeg())) {
            throw new IllegalArgumentException(
                    "This restaurant is marked Pure Veg. Non-veg menu items are not allowed."
            );
        }

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setPrice(request.getPrice());
        existing.setVeg(request.getVeg());
        existing.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : existing.getIsAvailable());

        return mapToMenuItemResponse(menuItemRepository.save(existing));
    }

    @Override
    public MenuItemResponse updateMenuItemImage(Long itemId, MultipartFile image) throws IOException {
        validateImageFile(image);

        MenuItem existing = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        existing.setImage(image.getBytes());
        return mapToMenuItemResponse(menuItemRepository.save(existing));
    }

    @Override
    public void deleteMenuItemImage(Long itemId) {
        MenuItem existing = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        existing.setImage(null);
        menuItemRepository.save(existing);
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

    private void validateNoDuplicateBranch(String name, Double latitude, Double longitude, Long excludeId) {
        if (name == null || latitude == null || longitude == null) {
            return;
        }

        List<Restaurant> sameName = restaurantRepository.findByNameIgnoreCase(name);

        for (Restaurant existing : sameName) {
            if (excludeId != null && existing.getId().equals(excludeId)) {
                continue;
            }

            if (existing.getLatitude() == null || existing.getLongitude() == null) {
                continue;
            }

            double distance = calculateHaversineDistance(
                    latitude, longitude,
                    existing.getLatitude(), existing.getLongitude()
            );

            if (distance <= DUPLICATE_BRANCH_RADIUS_KM) {
                throw new DuplicateResourceException(
                        "A branch of '" + name + "' already exists within 500 meters of this location."
                );
            }
        }
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file must not be empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Unsupported image format. Allowed: JPEG, JPG, PNG, WEBP");
        }
    }

    private RestaurantResponse mapToRestaurantResponse(Restaurant r) {
        return RestaurantResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .location(r.getLocation())
                .cuisine(r.getCuisine())
                .rating(r.getRating())
                .ratingCount(r.getRatingCount())
                .isActive(r.getIsActive())
                .isPureVeg(r.getIsPureVeg())
                .latitude(r.getLatitude())
                .longitude(r.getLongitude())
                .managerId(r.getManagerId())
                .image(r.getImage() != null
                        ? "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(r.getImage())
                        : null)
                .build();
    }

    private MenuItemResponse mapToMenuItemResponse(MenuItem m) {
        return MenuItemResponse.builder()
                .id(m.getId())
                .restaurantId(m.getRestaurant().getId())
                .name(m.getName())
                .description(m.getDescription())
                .price(m.getPrice())
                .image(m.getImage() != null
                        ? "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(m.getImage())
                        : null)
                .veg(m.getVeg())
                .isAvailable(m.getIsAvailable())
                .build();
    }
}