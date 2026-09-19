package com.fooddelivery.orderservice.service;

import com.fooddelivery.orderservice.dto.*;
import com.fooddelivery.orderservice.entity.Coupon;
import com.fooddelivery.orderservice.entity.Order;
import com.fooddelivery.orderservice.entity.OrderDeliveryAddress;
import com.fooddelivery.orderservice.entity.OrderItem;
import com.fooddelivery.orderservice.enums.CouponScope;
import com.fooddelivery.orderservice.enums.OrderStatus;
import com.fooddelivery.orderservice.enums.PaymentMethod;
import com.fooddelivery.orderservice.enums.PaymentStatus;
import com.fooddelivery.orderservice.repository.CouponRepository;
import com.fooddelivery.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CouponRepository couponRepository;
    private final CouponServiceImpl couponService;

    @Value("${app.delivery.charge}")
    private Double deliveryCharge;

    @Value("${app.delivery.preparation-buffer-minutes:12}")
    private Integer defaultPreparationBufferMinutes;

    @Override
    public OrderResponse placeOrder(Long userId, String email, PlaceOrderRequest request) {

        if (request.getPaymentMethod() == null) {
            throw new RuntimeException("Payment method is required");
        }

        if (request.getDeliveryAddress() == null) {
            throw new RuntimeException("Delivery address is required");
        }

        double originalAmount = request.getItems().stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        double discountAmount = 0.0;
        double delivery = deliveryCharge;
        String appliedCoupon = null;

        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            Coupon coupon = couponRepository
                    .findByCodeIgnoreCase(request.getCouponCode())
                    .orElseThrow(() -> new RuntimeException("Invalid coupon code"));

            if (!coupon.isActive()) {
                throw new RuntimeException("Coupon is not active");
            }

            if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDate.now())) {
                throw new RuntimeException("Coupon has expired");
            }

            if (originalAmount < coupon.getMinOrderAmount()) {
                throw new RuntimeException("Minimum order ₹" + coupon.getMinOrderAmount() + " required");
            }

            if (coupon.getScope() == CouponScope.RESTAURANT &&
                    !coupon.getRestaurantId().equals(request.getRestaurantId())) {
                throw new RuntimeException("Coupon not valid for this restaurant");
            }

            discountAmount = couponService.calculateDiscount(coupon, originalAmount);

            if (coupon.getDiscountType().name().equals("FREE_DELIVERY")) {
                delivery = 0.0;
            }

            appliedCoupon = coupon.getCode();
        }

        double totalAmount = originalAmount - discountAmount + delivery;

        PaymentStatus paymentStatus = request.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY
                ? PaymentStatus.PENDING
                : PaymentStatus.PAID;

        OrderRestaurantSnapshotRequest snapshot = request.getRestaurantSnapshot();
        int baseEtaMinutes = sanitizeMinutes(snapshot != null ? snapshot.getEstimatedMinutes() : null, 20);
        int prepBufferMinutes = sanitizeMinutes(defaultPreparationBufferMinutes, 12);
        int finalEtaMinutes = baseEtaMinutes + prepBufferMinutes;
        LocalDateTime etaTime = LocalDateTime.now().plusMinutes(finalEtaMinutes);

        Order order = Order.builder()
                .userId(userId)
                .userEmail(email)
                .restaurantId(request.getRestaurantId())
                .restaurantName(request.getRestaurantName())
                .restaurantLocation(snapshot != null ? snapshot.getLocation() : null)
                .restaurantCuisine(snapshot != null ? snapshot.getCuisine() : null)
                .restaurantLatitude(snapshot != null ? snapshot.getLatitude() : null)
                .restaurantLongitude(snapshot != null ? snapshot.getLongitude() : null)
                .restaurantDistanceKm(snapshot != null ? snapshot.getDistanceKm() : null)
                .restaurantEstimatedMinutes(baseEtaMinutes)
                .preparationBufferMinutes(prepBufferMinutes)
                .finalEstimatedDeliveryMinutes(finalEtaMinutes)
                .estimatedDeliveryAt(etaTime)
                .originalAmount(originalAmount)
                .discountAmount(discountAmount)
                .deliveryCharge(delivery)
                .totalAmount(totalAmount)
                .couponCode(appliedCoupon)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(paymentStatus)
                .status(OrderStatus.PLACED)
                .deliveryAddress(mapDeliveryAddress(request.getDeliveryAddress()))
                .build();

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> items = request.getItems().stream()
                .map(i -> OrderItem.builder()
                        .order(savedOrder)
                        .menuItemId(i.getMenuItemId())
                        .itemName(i.getItemName())
                        .price(i.getPrice())
                        .quantity(i.getQuantity())
                        .subtotal(i.getPrice() * i.getQuantity())
                        .build())
                .collect(Collectors.toList());

        savedOrder.setItems(items);
        orderRepository.save(savedOrder);

        return mapToOrderResponse(savedOrder);
    }

    @Override
    public List<OrderSummaryResponse> getMyOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        return mapToOrderResponse(order);
    }

    @Override
    public List<OrderSummaryResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        if (request.getStatus() == null) {
            throw new RuntimeException("Status is required");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(request.getStatus());
        applyEtaForStatus(order, request.getStatus());

        return mapToOrderResponse(orderRepository.save(order));
    }

    private void applyEtaForStatus(Order order, OrderStatus status) {
        int baseTravelMinutes = sanitizeMinutes(order.getRestaurantEstimatedMinutes(), 20);
        int prepBufferMinutes = sanitizeMinutes(order.getPreparationBufferMinutes(), 12);

        switch (status) {
            case PLACED ->
                    order.setEstimatedDeliveryAt(LocalDateTime.now().plusMinutes(baseTravelMinutes + prepBufferMinutes));
            case CONFIRMED ->
                    order.setEstimatedDeliveryAt(LocalDateTime.now().plusMinutes(baseTravelMinutes + Math.max(6, prepBufferMinutes - 3)));
            case PREPARING ->
                    order.setEstimatedDeliveryAt(LocalDateTime.now().plusMinutes(baseTravelMinutes + Math.max(4, prepBufferMinutes / 2)));
            case PICKED_UP, OUT_FOR_DELIVERY ->
                    order.setEstimatedDeliveryAt(LocalDateTime.now().plusMinutes(baseTravelMinutes));
            case DELIVERED, CANCELLED -> order.setEstimatedDeliveryAt(LocalDateTime.now());
        }
    }

    private int sanitizeMinutes(Integer value, int fallback) {
        if (value == null || value <= 0) {
            return fallback;
        }
        return value;
    }


    // ─── statuses where cancellation is still free / address+contact edits allowed ───
    private static final List<OrderStatus> EDITABLE_STATUSES = List.of(OrderStatus.PLACED, OrderStatus.CONFIRMED);
    private static final List<OrderStatus> TERMINAL_STATUSES = List.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED);
    private static final double ADDRESS_DELTA_BUFFER_KM = 2.0;
    private static final double MAX_DELIVERY_DISTANCE_KM = 15.0;

    @Override
    public OrderResponse cancelOrder(Long orderId, Long userId, CancelOrderRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        if (TERMINAL_STATUSES.contains(order.getStatus())) {
            throw new RuntimeException("This order can no longer be cancelled");
        }

        boolean isFreeCancellation = EDITABLE_STATUSES.contains(order.getStatus());
        double refund = 0.0;

        if (isFreeCancellation && order.getPaymentStatus() == PaymentStatus.PAID) {
            refund = order.getTotalAmount();
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledBy(request.getCancelledBy());
        order.setCancelledAt(LocalDateTime.now());
        order.setRefundAmount(refund);

        if (refund > 0) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        return mapToOrderResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse updateOrderAddress(Long orderId, Long userId, UpdateOrderAddressRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        if (!EDITABLE_STATUSES.contains(order.getStatus())) {
            throw new RuntimeException("Order is being prepared — delivery address can no longer be changed");
        }

        if (order.getRestaurantLatitude() == null || order.getRestaurantLongitude() == null
                || order.getRestaurantDistanceKm() == null) {
            throw new RuntimeException("Unable to verify delivery distance for this order");
        }

        double newDistanceKm = haversineKm(
                order.getRestaurantLatitude(),
                order.getRestaurantLongitude(),
                request.getLatitude(),
                request.getLongitude()
        );

        double allowedCeiling = Math.min(
                order.getRestaurantDistanceKm() + ADDRESS_DELTA_BUFFER_KM,
                MAX_DELIVERY_DISTANCE_KM
        );

        if (newDistanceKm > allowedCeiling) {
            throw new RuntimeException(
                    "New address is " + String.format("%.1f", newDistanceKm) +
                            " km away — you can only switch within " + String.format("%.1f", allowedCeiling) +
                            " km of your current delivery distance"
            );
        }

        OrderDeliveryAddress updatedAddress = OrderDeliveryAddress.builder()
                .addressId(request.getAddressId())
                .label(request.getLabel())
                .customLabel(request.getCustomLabel())
                .receiverName(request.getReceiverName())
                .phoneNumber(request.getPhoneNumber())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .landmark(request.getLandmark())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .isDefault(order.getDeliveryAddress() != null ? order.getDeliveryAddress().getIsDefault() : null)
                .build();

        order.setDeliveryAddress(updatedAddress);
        order.setRestaurantDistanceKm(newDistanceKm);

        return mapToOrderResponse(orderRepository.save(order));
    }

    //    @Override
//    public OrderResponse updateOrderContact(Long orderId, Long userId, UpdateOrderContactRequest request) {
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new RuntimeException("Order not found"));
//
//        if (!order.getUserId().equals(userId)) {
//            throw new RuntimeException("Access denied");
//        }
//
//        if (!EDITABLE_STATUSES.contains(order.getStatus())) {
//            throw new RuntimeException("Order is being prepared — contact details can no longer be changed");
//        }
//
//        if (order.getDeliveryAddress() == null) {
//            throw new RuntimeException("Delivery address not found for this order");
//        }
//
//        order.getDeliveryAddress().setReceiverName(request.getReceiverName());
//        order.getDeliveryAddress().setPhoneNumber(request.getPhoneNumber());
//
//        return mapToOrderResponse(orderRepository.save(order));
//    }
    @Override
    public OrderResponse updateOrderContact(Long orderId, Long userId, UpdateOrderContactRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        if (!EDITABLE_STATUSES.contains(order.getStatus())) {
            throw new RuntimeException("Order is being prepared — contact details can no longer be changed");
        }

        if (order.getDeliveryAddress() == null) {
            throw new RuntimeException("Delivery address not found");
        }

        String existingName = order.getDeliveryAddress().getReceiverName() == null
                ? ""
                : order.getDeliveryAddress().getReceiverName().trim();

        String existingPhone = order.getDeliveryAddress().getPhoneNumber() == null
                ? ""
                : order.getDeliveryAddress().getPhoneNumber().trim();

        String newName = request.getReceiverName() == null ? "" : request.getReceiverName().trim();
        String newPhone = request.getPhoneNumber() == null ? "" : request.getPhoneNumber().trim();

        boolean sameName = existingName.equals(newName);
        boolean samePhone = existingPhone.equals(newPhone);

        if (sameName && samePhone) {
            throw new RuntimeException("Same contact details cannot be updated");
        }

        order.getDeliveryAddress().setReceiverName(newName);
        order.getDeliveryAddress().setPhoneNumber(newPhone);

        return mapToOrderResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse updateOrderInstructions(Long orderId, Long userId, UpdateOrderInstructionsRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        if (!EDITABLE_STATUSES.contains(order.getStatus())) {
            throw new RuntimeException("Order is being prepared — cooking instructions can no longer be changed");
        }

        order.setCookingInstructions(request.getCookingInstructions());

        return mapToOrderResponse(orderRepository.save(order));
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return r * c;
    }

    private OrderDeliveryAddress mapDeliveryAddress(OrderDeliveryAddressRequest req) {
        return OrderDeliveryAddress.builder()
                .addressId(req.getAddressId())
                .label(req.getLabel())
                .customLabel(req.getCustomLabel())
                .receiverName(req.getReceiverName())
                .phoneNumber(req.getPhoneNumber())
                .addressLine1(req.getAddressLine1())
                .addressLine2(req.getAddressLine2())
                .landmark(req.getLandmark())
                .city(req.getCity())
                .state(req.getState())
                .postalCode(req.getPostalCode())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .isDefault(req.getIsDefault())
                .build();
    }

    private OrderResponse mapToOrderResponse(Order o) {
        List<OrderItemResponse> itemResponses = o.getItems() == null ? List.of() :
                o.getItems().stream()
                        .map(i -> OrderItemResponse.builder()
                                .menuItemId(i.getMenuItemId())
                                .itemName(i.getItemName())
                                .price(i.getPrice())
                                .quantity(i.getQuantity())
                                .subtotal(i.getSubtotal())
                                .build())
                        .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(o.getId())
                .userId(o.getUserId())
                .userEmail(o.getUserEmail())
                .restaurantId(o.getRestaurantId())
                .restaurantName(o.getRestaurantName())
                .restaurantLocation(o.getRestaurantLocation())
                .restaurantCuisine(o.getRestaurantCuisine())
                .restaurantLatitude(o.getRestaurantLatitude())
                .restaurantLongitude(o.getRestaurantLongitude())
                .restaurantDistanceKm(o.getRestaurantDistanceKm())
                .restaurantEstimatedMinutes(o.getRestaurantEstimatedMinutes())
                .preparationBufferMinutes(o.getPreparationBufferMinutes())
                .finalEstimatedDeliveryMinutes(o.getFinalEstimatedDeliveryMinutes())
                .estimatedDeliveryAt(o.getEstimatedDeliveryAt())
                .originalAmount(o.getOriginalAmount())
                .discountAmount(o.getDiscountAmount())
                .deliveryCharge(o.getDeliveryCharge())
                .totalAmount(o.getTotalAmount())
                .couponCode(o.getCouponCode())
                .paymentMethod(o.getPaymentMethod())
                .paymentStatus(o.getPaymentStatus())
                .status(o.getStatus())
                .cancelledBy(o.getCancelledBy())
                .cancelledAt(o.getCancelledAt())
                .refundAmount(o.getRefundAmount())
                .createdAt(o.getCreatedAt())
                .items(itemResponses)
                .deliveryAddress(o.getDeliveryAddress() == null ? null :
                        OrderDeliveryAddressResponse.builder()
                                .addressId(o.getDeliveryAddress().getAddressId())
                                .label(o.getDeliveryAddress().getLabel())
                                .customLabel(o.getDeliveryAddress().getCustomLabel())
                                .receiverName(o.getDeliveryAddress().getReceiverName())
                                .phoneNumber(o.getDeliveryAddress().getPhoneNumber())
                                .addressLine1(o.getDeliveryAddress().getAddressLine1())
                                .addressLine2(o.getDeliveryAddress().getAddressLine2())
                                .landmark(o.getDeliveryAddress().getLandmark())
                                .city(o.getDeliveryAddress().getCity())
                                .state(o.getDeliveryAddress().getState())
                                .postalCode(o.getDeliveryAddress().getPostalCode())
                                .latitude(o.getDeliveryAddress().getLatitude())
                                .longitude(o.getDeliveryAddress().getLongitude())
                                .isDefault(o.getDeliveryAddress().getIsDefault())
                                .build())
                .cookingInstructions(o.getCookingInstructions())
                .build();
    }

    private OrderSummaryResponse mapToSummary(Order o) {
        return OrderSummaryResponse.builder()
                .id(o.getId())
                .restaurantName(o.getRestaurantName())
                .restaurantLocation(o.getRestaurantLocation())
                .restaurantDistanceKm(o.getRestaurantDistanceKm())
                .restaurantEstimatedMinutes(o.getRestaurantEstimatedMinutes())
                .preparationBufferMinutes(o.getPreparationBufferMinutes())
                .finalEstimatedDeliveryMinutes(o.getFinalEstimatedDeliveryMinutes())
                .estimatedDeliveryAt(o.getEstimatedDeliveryAt())
                .totalAmount(o.getTotalAmount())
                .couponCode(o.getCouponCode())
                .paymentMethod(o.getPaymentMethod())
                .paymentStatus(o.getPaymentStatus())
                .status(o.getStatus())
                .createdAt(o.getCreatedAt())
                .build();
    }


    OrderResponse mapToOrderResponseInternal(Order o) {
        return mapToOrderResponse(o);
    }
}