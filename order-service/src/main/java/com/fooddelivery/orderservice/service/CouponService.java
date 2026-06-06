package com.fooddelivery.orderservice.service;

import com.fooddelivery.orderservice.dto.ApplyCouponRequest;
import com.fooddelivery.orderservice.dto.CreateCouponRequest;
import com.fooddelivery.orderservice.dto.ApplyCouponResponse;
import com.fooddelivery.orderservice.dto.CouponResponse;
import java.util.List;

public interface CouponService {
    CouponResponse createCoupon(CreateCouponRequest request);
    List<CouponResponse> getAllCoupons();
    List<CouponResponse> getCouponsForRestaurant(Long restaurantId);
    List<CouponResponse> getGlobalCoupons();
    ApplyCouponResponse applyCoupon(ApplyCouponRequest request);
    CouponResponse updateCoupon(Long id, CreateCouponRequest request);
    void deleteCoupon(Long id);
}