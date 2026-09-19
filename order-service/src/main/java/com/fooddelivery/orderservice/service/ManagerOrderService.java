package com.fooddelivery.orderservice.service;

import com.fooddelivery.orderservice.dto.OrderResponse;
import com.fooddelivery.orderservice.dto.UpdateOrderStatusRequest;

import java.util.List;

public interface ManagerOrderService {

    List<OrderResponse> getOrdersForManager(Long managerId);

    OrderResponse getOrderByIdForManager(Long managerId, Long orderId);

    OrderResponse updateOrderStatusForManager(
            Long managerId,
            Long orderId,
            UpdateOrderStatusRequest request
    );
}