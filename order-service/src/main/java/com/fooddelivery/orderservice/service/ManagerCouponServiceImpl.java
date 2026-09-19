package com.fooddelivery.orderservice.service;

import com.fooddelivery.orderservice.client.RestaurantClient;
import com.fooddelivery.orderservice.dto.CouponResponse;
import com.fooddelivery.orderservice.dto.CreateCouponRequest;
import com.fooddelivery.orderservice.entity.Coupon;
import com.fooddelivery.orderservice.enums.CouponScope;
import com.fooddelivery.orderservice.enums.DiscountType;
import com.fooddelivery.orderservice.exception.ResourceNotFoundException;
import com.fooddelivery.orderservice.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ManagerCouponServiceImpl implements ManagerCouponService {

    private final CouponRepository couponRepository;
    private final RestaurantClient restaurantClient;

    @Override
    public CouponResponse createCoupon(
            Long managerId,
            CreateCouponRequest request
    ) {
        validateManagerCouponRequest(request);

        Long restaurantId = request.getRestaurantId();

        validateManagerOwnsRestaurant(managerId, restaurantId);

        String couponCode = request.getCode()
                .trim()
                .toUpperCase();

        if (couponRepository.findByCodeIgnoreCase(couponCode).isPresent()) {
            throw new IllegalArgumentException(
                    "Coupon code already exists. Please use a different code."
            );
        }

        Coupon coupon = Coupon.builder()
                .code(couponCode)
                .description(normalizeDescription(request.getDescription()))
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxDiscountAmount(
                        getAllowedMaxDiscountAmount(request)
                )
                .minOrderAmount(request.getMinOrderAmount())
                .scope(CouponScope.RESTAURANT)
                .restaurantId(restaurantId)
                .active(request.isActive())
                .expiryDate(request.getExpiryDate())
                .build();

        Coupon savedCoupon = couponRepository.save(coupon);

        return mapToResponse(savedCoupon);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponResponse> getCouponsForManager(
            Long managerId,
            Long restaurantId
    ) {
        List<Long> managerRestaurantIds =
                getManagerRestaurantIds(managerId);

        if (restaurantId != null) {
            if (!managerRestaurantIds.contains(restaurantId)) {
                throw new ResourceNotFoundException(
                        "Restaurant not found or you do not have access to its coupons."
                );
            }

            return couponRepository
                    .findByScopeAndRestaurantId(
                            CouponScope.RESTAURANT,
                            restaurantId
                    )
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        if (managerRestaurantIds.isEmpty()) {
            return List.of();
        }

        return couponRepository
                .findByScopeAndRestaurantIdIn(
                        CouponScope.RESTAURANT,
                        managerRestaurantIds
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public CouponResponse updateCoupon(
            Long managerId,
            Long couponId,
            CreateCouponRequest request
    ) {
        validateManagerCouponRequest(request);

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Coupon not found with ID: " + couponId
                ));

        /*
         * Validate the restaurant currently linked to the coupon.
         * This prevents a manager from editing another manager's coupon.
         */
        validateManagerOwnsRestaurant(
                managerId,
                coupon.getRestaurantId()
        );

        /*
         * Validate the restaurant coming from the update payload too.
         * This safely permits moving a coupon only between restaurants
         * owned by the same manager.
         */
        validateManagerOwnsRestaurant(
                managerId,
                request.getRestaurantId()
        );

        String requestedCode = request.getCode()
                .trim()
                .toUpperCase();

        boolean couponCodeChanged =
                !coupon.getCode().equalsIgnoreCase(requestedCode);

        if (couponCodeChanged &&
                couponRepository.findByCodeIgnoreCase(requestedCode).isPresent()) {
            throw new IllegalArgumentException(
                    "Coupon code already exists. Please use a different code."
            );
        }

        coupon.setCode(requestedCode);
        coupon.setDescription(
                normalizeDescription(request.getDescription())
        );
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMaxDiscountAmount(
                getAllowedMaxDiscountAmount(request)
        );
        coupon.setMinOrderAmount(request.getMinOrderAmount());

        /*
         * A restaurant manager can never create or convert a coupon
         * to GLOBAL scope.
         */
        coupon.setScope(CouponScope.RESTAURANT);

        coupon.setRestaurantId(request.getRestaurantId());
        coupon.setActive(request.isActive());
        coupon.setExpiryDate(request.getExpiryDate());

        Coupon updatedCoupon = couponRepository.save(coupon);

        return mapToResponse(updatedCoupon);
    }

    @Override
    public void deleteCoupon(
            Long managerId,
            Long couponId
    ) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Coupon not found with ID: " + couponId
                ));

        validateManagerOwnsRestaurant(
                managerId,
                coupon.getRestaurantId()
        );

        couponRepository.delete(coupon);
    }

    private List<Long> getManagerRestaurantIds(Long managerId) {
        try {
            List<Long> restaurantIds =
                    restaurantClient.getRestaurantIdsByManagerId(managerId);

            return restaurantIds != null
                    ? restaurantIds
                    : List.of();

        } catch (Exception ex) {
            log.error(
                    "Failed to fetch owned restaurant IDs for managerId={}",
                    managerId,
                    ex
            );

            throw new IllegalStateException(
                    "Unable to verify restaurant ownership",
                    ex
            );
        }
    }

    private void validateManagerOwnsRestaurant(
            Long managerId,
            Long restaurantId
    ) {
        if (restaurantId == null) {
            throw new IllegalArgumentException(
                    "Restaurant ID is required for a manager coupon."
            );
        }

        List<Long> managerRestaurantIds =
                getManagerRestaurantIds(managerId);

        if (!managerRestaurantIds.contains(restaurantId)) {
            throw new ResourceNotFoundException(
                    "Restaurant not found or you do not have access to its coupons."
            );
        }
    }

    private void validateManagerCouponRequest(
            CreateCouponRequest request
    ) {
        if (request.getRestaurantId() == null) {
            throw new IllegalArgumentException(
                    "Restaurant ID is required for manager coupons."
            );
        }

        if (request.getScope() != CouponScope.RESTAURANT) {
            throw new IllegalArgumentException(
                    "Managers can create and manage only RESTAURANT coupons."
            );
        }

        if (request.getCode() == null ||
                request.getCode().trim().isBlank()) {
            throw new IllegalArgumentException(
                    "Coupon code is required."
            );
        }

        if (request.getDiscountType() == null) {
            throw new IllegalArgumentException(
                    "Discount type is required."
            );
        }

        validateDiscountValues(request);

        if (request.getMinOrderAmount() == null ||
                request.getMinOrderAmount() < 0) {
            throw new IllegalArgumentException(
                    "Minimum order amount cannot be negative."
            );
        }
    }

    private void validateDiscountValues(
            CreateCouponRequest request
    ) {
        DiscountType discountType = request.getDiscountType();
        Double discountValue = request.getDiscountValue();
        Double maxDiscountAmount = request.getMaxDiscountAmount();

        if (discountType == DiscountType.FREE_DELIVERY) {
            if (discountValue == null || discountValue != 0) {
                throw new IllegalArgumentException(
                        "Discount value must be 0 for a free delivery coupon."
                );
            }

            if (maxDiscountAmount != null) {
                throw new IllegalArgumentException(
                        "Maximum discount amount is not allowed for a free delivery coupon."
                );
            }

            return;
        }

        if (discountValue == null || discountValue <= 0) {
            throw new IllegalArgumentException(
                    "Discount value must be greater than zero."
            );
        }

        if (discountType == DiscountType.PERCENTAGE &&
                discountValue > 100) {
            throw new IllegalArgumentException(
                    "Percentage discount cannot be greater than 100."
            );
        }

        if (discountType == DiscountType.FLAT &&
                maxDiscountAmount != null) {
            throw new IllegalArgumentException(
                    "Maximum discount amount is allowed only for percentage coupons."
            );
        }

        if (maxDiscountAmount != null &&
                maxDiscountAmount <= 0) {
            throw new IllegalArgumentException(
                    "Maximum discount amount must be greater than zero."
            );
        }
    }

    private Double getAllowedMaxDiscountAmount(
            CreateCouponRequest request
    ) {
        return request.getDiscountType() == DiscountType.PERCENTAGE
                ? request.getMaxDiscountAmount()
                : null;
    }

    private String normalizeDescription(String description) {
        if (description == null || description.trim().isBlank()) {
            return null;
        }

        return description.trim();
    }

    private CouponResponse mapToResponse(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .minOrderAmount(coupon.getMinOrderAmount())
                .scope(coupon.getScope())
                .restaurantId(coupon.getRestaurantId())
                .active(coupon.isActive())
                .expiryDate(coupon.getExpiryDate())
                .build();
    }
}