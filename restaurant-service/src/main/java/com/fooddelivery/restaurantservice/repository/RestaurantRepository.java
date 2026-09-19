package com.fooddelivery.restaurantservice.repository;

import com.fooddelivery.restaurantservice.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    Optional<Restaurant> findByNameIgnoreCaseAndLocationIgnoreCase(String name, String location);

    List<Restaurant> findByLocationContainingIgnoreCaseAndCuisineContainingIgnoreCase(
            String location, String cuisine);

    List<Restaurant> findByIsActiveTrueAndLocationContainingIgnoreCaseAndCuisineContainingIgnoreCase(
            String location, String cuisine);

    List<Restaurant> findByNameIgnoreCase(String name);

    @Query("SELECT r FROM Restaurant r WHERE r.latitude BETWEEN :minLat AND :maxLat " +
            "AND r.longitude BETWEEN :minLng AND :maxLng")
    List<Restaurant> findWithinBoundingBox(
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLng") double minLng,
            @Param("maxLng") double maxLng
    );

    List<Restaurant> findByManagerIdOrderByIdDesc(Long managerId);

    List<Restaurant> findByManagerIdAndIsActiveTrueOrderByIdDesc(Long managerId);

    Optional<Restaurant> findByIdAndManagerId(Long id, Long managerId);

    Long countByManagerId(Long managerId);
    Long countByManagerIdAndIsActiveTrue(Long managerId);
    Long countByManagerIdAndIsActiveFalse(Long managerId);

    List<Restaurant> findByManagerIdOrderByNameAsc(Long managerId);
    List<Restaurant> findByManagerIdAndIsActiveTrueOrderByNameAsc(Long managerId);
    List<Restaurant> findByManagerIdAndIsActiveFalseOrderByNameAsc(Long managerId);
}