package com.fooddelivery.orderservice.controller;

import com.fooddelivery.orderservice.dto.CouponResponse;
import com.fooddelivery.orderservice.dto.CreateCouponRequest;
import com.fooddelivery.orderservice.service.ManagerCouponService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager/coupons")
@RequiredArgsConstructor
public class ManagerCouponController {

    private final ManagerCouponService managerCouponService;

    @PostMapping
    public ResponseEntity<CouponResponse> createCoupon(
            @Valid @RequestBody CreateCouponRequest request,
            HttpServletRequest httpRequest
    ) {
        Long managerId = getManagerId(httpRequest);

        CouponResponse response = managerCouponService.createCoupon(
                managerId,
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<CouponResponse>> getMyCoupons(
            @RequestParam(required = false) Long restaurantId,
            HttpServletRequest httpRequest
    ) {
        Long managerId = getManagerId(httpRequest);

        return ResponseEntity.ok(
                managerCouponService.getCouponsForManager(
                        managerId,
                        restaurantId
                )
        );
    }

    @PutMapping("/{couponId}")
    public ResponseEntity<CouponResponse> updateCoupon(
            @PathVariable Long couponId,
            @Valid @RequestBody CreateCouponRequest request,
            HttpServletRequest httpRequest
    ) {
        Long managerId = getManagerId(httpRequest);

        return ResponseEntity.ok(
                managerCouponService.updateCoupon(
                        managerId,
                        couponId,
                        request
                )
        );
    }

    @DeleteMapping("/{couponId}")
    public ResponseEntity<Void> deleteCoupon(
            @PathVariable Long couponId,
            HttpServletRequest httpRequest
    ) {
        Long managerId = getManagerId(httpRequest);

        managerCouponService.deleteCoupon(managerId, couponId);

        return ResponseEntity.noContent().build();
    }

    private Long getManagerId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");

        if (userId == null) {
            throw new RuntimeException("Authenticated manager ID is missing");
        }

        if (userId instanceof Long managerId) {
            return managerId;
        }

        return Long.valueOf(userId.toString());
    }
}