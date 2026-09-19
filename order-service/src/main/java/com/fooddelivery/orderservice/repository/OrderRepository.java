package com.fooddelivery.orderservice.repository;

import com.fooddelivery.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Order> findByRestaurantIdIn(List<Long> restaurantIds);

    Optional<Order> findByIdAndRestaurantIdIn(Long id, List<Long> restaurantIds);
}