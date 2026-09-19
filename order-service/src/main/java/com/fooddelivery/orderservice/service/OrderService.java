package com.fooddelivery.orderservice.service;

import com.fooddelivery.orderservice.dto.*;
import java.util.List;

public interface OrderService {
    OrderResponse placeOrder(Long userId, String email, PlaceOrderRequest request);
    List<OrderSummaryResponse> getMyOrders(Long userId);
    OrderResponse getOrderById(Long orderId, Long userId);
    List<OrderSummaryResponse> getAllOrders();
    OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request);

    OrderResponse cancelOrder(Long orderId, Long userId, CancelOrderRequest request);
    OrderResponse updateOrderAddress(Long orderId, Long userId, UpdateOrderAddressRequest request);
    OrderResponse updateOrderContact(Long orderId, Long userId, UpdateOrderContactRequest request);
    OrderResponse updateOrderInstructions(Long orderId, Long userId, UpdateOrderInstructionsRequest request);

}