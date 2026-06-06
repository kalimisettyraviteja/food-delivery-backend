package com.fooddelivery.orderservice.service;

import com.fooddelivery.orderservice.dto.PlaceOrderRequest;
import com.fooddelivery.orderservice.dto.UpdateOrderStatusRequest;
import com.fooddelivery.orderservice.dto.*;
import com.fooddelivery.orderservice.entity.Coupon;
import com.fooddelivery.orderservice.entity.Order;
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

    @Override
    public OrderResponse placeOrder(Long userId, String email, PlaceOrderRequest request) {

        // Validate payment method
        if (request.getPaymentMethod() == null)
            throw new RuntimeException("Payment method is required");

        // Calculate original amount from items
        double originalAmount = request.getItems().stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        double discountAmount = 0.0;
        double delivery       = deliveryCharge;
        String appliedCoupon  = null;

        // Apply coupon if provided
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            Coupon coupon = couponRepository
                    .findByCodeIgnoreCase(request.getCouponCode())
                    .orElseThrow(() -> new RuntimeException("Invalid coupon code"));

            // Validate coupon
            if (!coupon.isActive())
                throw new RuntimeException("Coupon is not active");

            if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDate.now()))
                throw new RuntimeException("Coupon has expired");

            if (originalAmount < coupon.getMinOrderAmount())
                throw new RuntimeException("Minimum order ₹" + coupon.getMinOrderAmount() + " required");

            if (coupon.getScope() == CouponScope.RESTAURANT &&
                    !coupon.getRestaurantId().equals(request.getRestaurantId()))
                throw new RuntimeException("Coupon not valid for this restaurant");

            discountAmount = couponService.calculateDiscount(coupon, originalAmount);

            if (coupon.getDiscountType().name().equals("FREE_DELIVERY")) {
                delivery = 0.0;
            }
            appliedCoupon = coupon.getCode();
        }

        double totalAmount = originalAmount - discountAmount + delivery;

        // Determine payment status based on method
        PaymentStatus paymentStatus = request.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY
                ? PaymentStatus.PENDING
                : PaymentStatus.PAID;

        // Build order
        Order order = Order.builder()
                .userId(userId)
                .userEmail(email)
                .restaurantId(request.getRestaurantId())
                .restaurantName(request.getRestaurantName())
                .originalAmount(originalAmount)
                .discountAmount(discountAmount)
                .deliveryCharge(delivery)
                .totalAmount(totalAmount)
                .couponCode(appliedCoupon)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(paymentStatus)
                .status(OrderStatus.PLACED)
                .build();

        Order savedOrder = orderRepository.save(order);

        // Build order items
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
                .stream().map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUserId().equals(userId))
            throw new RuntimeException("Access denied");

        return mapToOrderResponse(order);
    }

    @Override
    public List<OrderSummaryResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream().map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(request.getStatus());
        return mapToOrderResponse(orderRepository.save(order));
    }

    // ── Mappers ──
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
                .originalAmount(o.getOriginalAmount())
                .discountAmount(o.getDiscountAmount())
                .deliveryCharge(o.getDeliveryCharge())
                .totalAmount(o.getTotalAmount())
                .couponCode(o.getCouponCode())
                .paymentMethod(o.getPaymentMethod())
                .paymentStatus(o.getPaymentStatus())
                .status(o.getStatus())
                .createdAt(o.getCreatedAt())
                .items(itemResponses)
                .build();
    }

    private OrderSummaryResponse mapToSummary(Order o) {
        return OrderSummaryResponse.builder()
                .id(o.getId())
                .restaurantName(o.getRestaurantName())
                .totalAmount(o.getTotalAmount())
                .couponCode(o.getCouponCode())
                .paymentMethod(o.getPaymentMethod())
                .paymentStatus(o.getPaymentStatus())
                .status(o.getStatus())
                .createdAt(o.getCreatedAt())
                .build();
    }
}