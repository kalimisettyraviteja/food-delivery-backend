package com.fooddelivery.orderservice.service;

import com.fooddelivery.orderservice.dto.CouponResponse;
import com.fooddelivery.orderservice.dto.CreateCouponRequest;

import java.util.List;

public interface ManagerCouponService {

    CouponResponse createCoupon(
            Long managerId,
            CreateCouponRequest request
    );

    List<CouponResponse> getCouponsForManager(
            Long managerId,
            Long restaurantId
    );

    CouponResponse updateCoupon(
            Long managerId,
            Long couponId,
            CreateCouponRequest request
    );

    void deleteCoupon(
            Long managerId,
            Long couponId
    );
}