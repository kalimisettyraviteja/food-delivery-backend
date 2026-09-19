package com.fooddelivery.restaurantservice.service;

import com.fooddelivery.restaurantservice.dto.MenuItemResponse;
import com.fooddelivery.restaurantservice.dto.RestaurantResponse;
import com.fooddelivery.restaurantservice.entity.MenuItem;
import com.fooddelivery.restaurantservice.entity.Restaurant;
import com.fooddelivery.restaurantservice.exception.ResourceNotFoundException;
import com.fooddelivery.restaurantservice.repository.MenuItemRepository;
import com.fooddelivery.restaurantservice.repository.RestaurantRepository;
import org.springframework.stereotype.Service;

import java.util.Base64;
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

    private static final double DEFAULT_SEARCH_RADIUS_KM = 15.0;

    @Override
    public List<RestaurantResponse> searchRestaurants(String location, String cuisine, Double userLat, Double userLng, Boolean pureVegOnly) {
        String loc = (location == null || location.isBlank()) ? "" : location;
        String cui = (cuisine == null || cuisine.isBlank()) ? "" : cuisine;

        List<Restaurant> restaurants;

        if (userLat != null && userLng != null) {
            double latDelta = DEFAULT_SEARCH_RADIUS_KM / 111.0;
            double lngDelta = DEFAULT_SEARCH_RADIUS_KM / (111.0 * Math.cos(Math.toRadians(userLat)));

            double minLat = userLat - latDelta;
            double maxLat = userLat + latDelta;
            double minLng = userLng - lngDelta;
            double maxLng = userLng + lngDelta;

            restaurants = restaurantRepository.findWithinBoundingBox(minLat, maxLat, minLng, maxLng);

            if (!loc.isBlank()) {
                restaurants = restaurants.stream()
                        .filter(r -> r.getLocation().toLowerCase().contains(loc.toLowerCase()))
                        .toList();
            }

            if (!cui.isBlank()) {
                restaurants = restaurants.stream()
                        .filter(r -> r.getCuisine().toLowerCase().contains(cui.toLowerCase()))
                        .toList();
            }

            if (Boolean.TRUE.equals(pureVegOnly)) {
                restaurants = restaurants.stream()
                        .filter(r -> Boolean.TRUE.equals(r.getIsPureVeg()))
                        .toList();
            }

            return restaurants.stream()
                    .map(r -> mapToRestaurantResponse(r, userLat, userLng))
                    .filter(r -> r.getDistanceKm() == null || r.getDistanceKm() <= DEFAULT_SEARCH_RADIUS_KM)
                    .toList();

        } else {
            restaurants = restaurantRepository
                    .findByLocationContainingIgnoreCaseAndCuisineContainingIgnoreCase(loc, cui);

            if (Boolean.TRUE.equals(pureVegOnly)) {
                restaurants = restaurants.stream()
                        .filter(r -> Boolean.TRUE.equals(r.getIsPureVeg()))
                        .toList();
            }

            return restaurants.stream()
                    .sorted(
                            java.util.Comparator.comparing(
                                    Restaurant::getRating,
                                    java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())
                            )
                    )
                    .limit(20)
                    .map(r -> mapToRestaurantResponse(r, null, null))
                    .toList();
        }
    }

    @Override
    public RestaurantResponse getRestaurantById(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        return mapToRestaurantResponse(restaurant, null, null);
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

    @Override
    public void updateRestaurantRating(Long restaurantId, Double rating, Integer ratingCount) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        restaurant.setRating(rating);
        restaurant.setRatingCount(ratingCount);
        restaurantRepository.save(restaurant);
    }

    private RestaurantResponse mapToRestaurantResponse(Restaurant r, Double userLat, Double userLng) {
        RestaurantResponse.RestaurantResponseBuilder builder = RestaurantResponse.builder()
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
                        : null);

        if (userLat != null && userLng != null && r.getLatitude() != null && r.getLongitude() != null) {
            double distanceKm = calculateHaversineDistance(userLat, userLng, r.getLatitude(), r.getLongitude());
            int estimatedMinutes = estimateDeliveryMinutes(distanceKm);

            builder.distanceKm(Math.round(distanceKm * 10.0) / 10.0)
                    .estimatedMinutes(estimatedMinutes);
        }

        return builder.build();
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

    private int estimateDeliveryMinutes(double distanceKm) {
        final double avgSpeedKmph = 20.0;
        final int prepTimeMinutes = 10;
        int travelMinutes = (int) Math.ceil((distanceKm / avgSpeedKmph) * 60);
        return travelMinutes + prepTimeMinutes;
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