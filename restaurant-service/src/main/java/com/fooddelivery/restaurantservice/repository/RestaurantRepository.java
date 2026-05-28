package com.fooddelivery.restaurantservice.repository;

import com.fooddelivery.restaurantservice.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    Optional<Restaurant> findByNameIgnoreCaseAndLocationIgnoreCase(String name, String location);

    List<Restaurant> findByLocationContainingIgnoreCaseAndCuisineContainingIgnoreCase(
            String location, String cuisine);

    List<Restaurant> findByIsActiveTrueAndLocationContainingIgnoreCaseAndCuisineContainingIgnoreCase(
            String location, String cuisine);

}