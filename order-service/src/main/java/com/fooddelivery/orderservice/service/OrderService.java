package com.fooddelivery.orderservice.service;

import com.fooddelivery.orderservice.dto.PlaceOrderRequest;
import com.fooddelivery.orderservice.dto.UpdateOrderStatusRequest;
import com.fooddelivery.orderservice.dto.OrderResponse;
import com.fooddelivery.orderservice.dto.OrderSummaryResponse;
import java.util.List;

public interface OrderService {
    OrderResponse placeOrder(Long userId, String email, PlaceOrderRequest request);
    List<OrderSummaryResponse> getMyOrders(Long userId);
    OrderResponse getOrderById(Long orderId, Long userId);
    List<OrderSummaryResponse> getAllOrders();
    OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request);
}