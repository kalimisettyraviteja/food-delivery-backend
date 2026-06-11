package com.fooddelivery.orderservice.repository;

import com.fooddelivery.orderservice.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    List<Review> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);

    @Query("select coalesce(avg(r.rating), 0) from Review r where r.restaurantId = :restaurantId")
    Double findAverageRatingByRestaurantId(Long restaurantId);

    @Query("select count(r) from Review r where r.restaurantId = :restaurantId")
    Long countByRestaurantId(Long restaurantId);
}