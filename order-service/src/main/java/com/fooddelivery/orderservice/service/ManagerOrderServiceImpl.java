package com.fooddelivery.orderservice.service;

import com.fooddelivery.orderservice.client.RestaurantClient;
import com.fooddelivery.orderservice.dto.OrderResponse;
import com.fooddelivery.orderservice.dto.UpdateOrderStatusRequest;
import com.fooddelivery.orderservice.entity.Order;
import com.fooddelivery.orderservice.enums.OrderStatus;
import com.fooddelivery.orderservice.exception.ResourceNotFoundException; // or RuntimeException
import com.fooddelivery.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagerOrderServiceImpl implements ManagerOrderService {

    private final OrderRepository orderRepository;
    private final RestaurantClient restaurantClient;
    private final OrderServiceImpl orderServiceImpl;


    @Override
    public List<OrderResponse> getOrdersForManager(Long managerId) {
        List<Long> restaurantIds = getManagerRestaurantIds(managerId);

        if (restaurantIds.isEmpty()) {
            return List.of();
        }

        List<Order> orders = orderRepository.findByRestaurantIdIn(restaurantIds);

        return orders.stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    @Override
    public OrderResponse getOrderByIdForManager(Long managerId, Long orderId) {
        List<Long> restaurantIds = getManagerRestaurantIds(managerId);

        Order order = orderRepository
                .findByIdAndRestaurantIdIn(orderId, restaurantIds)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found or you do not have access to it."
                ));

        return mapToOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatusForManager(
            Long managerId,
            Long orderId,
            UpdateOrderStatusRequest request
    ) {
        List<Long> restaurantIds = getManagerRestaurantIds(managerId);

        Order order = orderRepository
                .findByIdAndRestaurantIdIn(orderId, restaurantIds)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found or you do not have access to it."
                ));

        validateManagerStatusTransition(order.getStatus(), request.getStatus());

        order.setStatus(request.getStatus());
        applyEtaForStatus(order, request.getStatus());

        Order saved = orderRepository.save(order);
        return mapToOrderResponse(saved);
    }

    private List<Long> getManagerRestaurantIds(Long managerId) {
        try {
            return restaurantClient.getRestaurantIdsByManagerId(managerId);
        } catch (Exception ex) {
            log.error("Failed to fetch restaurant IDs for managerId={}", managerId, ex);
            throw new IllegalStateException(
                    "Unable to determine manager's restaurants", ex
            );
        }
    }

    private void validateManagerStatusTransition(OrderStatus current, OrderStatus next) {
        if (current == OrderStatus.CANCELLED || current == OrderStatus.DELIVERED) {
            throw new IllegalArgumentException(
                    "Cannot change status of an order that is already " + current
            );
        }

        // Optional: add more rules here if needed
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

//    private OrderResponse mapToOrderResponse(Order order) {
//        // Reuse your existing mapping logic from OrderServiceImpl
//        return new OrderServiceImpl(orderRepository, null, null)
//                .mapToOrderResponseInternal(order);
//    }

    private OrderResponse mapToOrderResponse(Order order) {
        return orderServiceImpl.mapToOrderResponseInternal(order);
    }
}