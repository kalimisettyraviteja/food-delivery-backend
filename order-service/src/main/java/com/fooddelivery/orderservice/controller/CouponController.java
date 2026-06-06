package com.fooddelivery.orderservice.controller;

import com.fooddelivery.orderservice.dto.ApplyCouponRequest;
import com.fooddelivery.orderservice.dto.ApplyCouponResponse;
import com.fooddelivery.orderservice.dto.CouponResponse;
import com.fooddelivery.orderservice.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<CouponResponse>> getForRestaurant(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(couponService.getCouponsForRestaurant(restaurantId));
    }

    @GetMapping("/global")
    public ResponseEntity<List<CouponResponse>> getGlobal() {
        return ResponseEntity.ok(couponService.getGlobalCoupons());
    }

    @PostMapping("/apply")
    public ResponseEntity<ApplyCouponResponse> applyCoupon(@Valid @RequestBody ApplyCouponRequest request) {
        return ResponseEntity.ok(couponService.applyCoupon(request));
    }
}