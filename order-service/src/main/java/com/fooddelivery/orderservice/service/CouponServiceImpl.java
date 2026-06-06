package com.fooddelivery.orderservice.service;

import com.fooddelivery.orderservice.dto.ApplyCouponRequest;
import com.fooddelivery.orderservice.dto.CreateCouponRequest;
import com.fooddelivery.orderservice.dto.ApplyCouponResponse;
import com.fooddelivery.orderservice.dto.CouponResponse;
import com.fooddelivery.orderservice.entity.Coupon;
import com.fooddelivery.orderservice.enums.CouponScope;
import com.fooddelivery.orderservice.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    @Value("${app.delivery.charge}")
    private Double deliveryCharge;

    @Override
    public CouponResponse createCoupon(CreateCouponRequest request) {
        Coupon coupon = Coupon.builder()
                .code(request.getCode().toUpperCase())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .minOrderAmount(request.getMinOrderAmount())
                .scope(request.getScope())
                .restaurantId(request.getRestaurantId())
                .active(request.isActive())
                .expiryDate(request.getExpiryDate())
                .build();

        return mapToResponse(couponRepository.save(coupon));
    }

    @Override
    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAll()
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CouponResponse> getCouponsForRestaurant(Long restaurantId) {
        List<Coupon> global = couponRepository
                .findByScopeAndActiveTrue(CouponScope.GLOBAL);
        List<Coupon> specific = couponRepository
                .findByScopeAndRestaurantIdAndActiveTrue(CouponScope.RESTAURANT, restaurantId);

        global.addAll(specific);
        return global.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<CouponResponse> getGlobalCoupons() {
        return couponRepository.findByScopeAndActiveTrue(CouponScope.GLOBAL)
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ApplyCouponResponse applyCoupon(ApplyCouponRequest request) {
        Coupon coupon = couponRepository
                .findByCodeIgnoreCase(request.getCouponCode())
                .orElseThrow(() -> new RuntimeException("Invalid coupon code"));

        // Active check
        if (!coupon.isActive()) {
            throw new RuntimeException("Coupon is no longer active");
        }

        // Expiry check
        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Coupon has expired");
        }

        // Min order check
        if (request.getOrderAmount() < coupon.getMinOrderAmount()) {
            throw new RuntimeException("Minimum order amount for this coupon is ₹" + coupon.getMinOrderAmount());
        }

        // Restaurant scope check
        if (coupon.getScope() == CouponScope.RESTAURANT &&
                !coupon.getRestaurantId().equals(request.getRestaurantId())) {
            throw new RuntimeException("Coupon not valid for this restaurant");
        }

        double discount    = calculateDiscount(coupon, request.getOrderAmount());
        double delivery    = coupon.getDiscountType().name().equals("FREE_DELIVERY") ? 0 : deliveryCharge;
        double finalAmount = request.getOrderAmount() - discount + delivery;

        return ApplyCouponResponse.builder()
                .couponCode(coupon.getCode())
                .description(coupon.getDescription())
                .originalAmount(request.getOrderAmount())
                .discountAmount(discount)
                .deliveryCharge(delivery)
                .finalAmount(finalAmount)
                .message("Coupon applied successfully!")
                .build();
    }

    @Override
    public CouponResponse updateCoupon(Long id, CreateCouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        coupon.setCode(request.getCode().toUpperCase());
        coupon.setDescription(request.getDescription());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMaxDiscountAmount(request.getMaxDiscountAmount());
        coupon.setMinOrderAmount(request.getMinOrderAmount());
        coupon.setScope(request.getScope());
        coupon.setRestaurantId(request.getRestaurantId());
        coupon.setActive(request.isActive());
        coupon.setExpiryDate(request.getExpiryDate());

        return mapToResponse(couponRepository.save(coupon));
    }

    @Override
    public void deleteCoupon(Long id) {
        couponRepository.deleteById(id);
    }

    // ── Discount calculation logic ──
    public double calculateDiscount(Coupon coupon, Double orderTotal) {
        return switch (coupon.getDiscountType()) {
            case FLAT -> coupon.getDiscountValue();
            case PERCENTAGE -> {
                double calc = orderTotal * coupon.getDiscountValue() / 100;
                yield coupon.getMaxDiscountAmount() != null
                        ? Math.min(calc, coupon.getMaxDiscountAmount())
                        : calc;
            }
            case FREE_DELIVERY -> 0.0;
        };
    }

    private CouponResponse mapToResponse(Coupon c) {
        return CouponResponse.builder()
                .id(c.getId())
                .code(c.getCode())
                .description(c.getDescription())
                .discountType(c.getDiscountType())
                .discountValue(c.getDiscountValue())
                .maxDiscountAmount(c.getMaxDiscountAmount())
                .minOrderAmount(c.getMinOrderAmount())
                .scope(c.getScope())
                .restaurantId(c.getRestaurantId())
                .active(c.isActive())
                .expiryDate(c.getExpiryDate())
                .build();
    }
}