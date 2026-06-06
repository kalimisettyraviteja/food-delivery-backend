package com.fooddelivery.orderservice.repository;

import com.fooddelivery.orderservice.entity.Coupon;
import com.fooddelivery.orderservice.enums.CouponScope;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCodeIgnoreCase(String code);
    List<Coupon> findByScopeAndActiveTrue(CouponScope scope);
    List<Coupon> findByScopeAndRestaurantIdAndActiveTrue(CouponScope scope, Long restaurantId);
}