package com.fooddelivery.orderservice.controller;

import com.fooddelivery.orderservice.dto.OrderResponse;
import com.fooddelivery.orderservice.dto.UpdateOrderStatusRequest;
import com.fooddelivery.orderservice.service.ManagerOrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager/orders")
@RequiredArgsConstructor
public class ManagerOrderController {

    private final ManagerOrderService managerOrderService;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(HttpServletRequest request) {
        Long managerId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(managerOrderService.getOrdersForManager(managerId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getMyOrder(
            @PathVariable Long orderId,
            HttpServletRequest request
    ) {
        Long managerId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(
                managerOrderService.getOrderByIdForManager(managerId, orderId)
        );
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateMyOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            HttpServletRequest httpRequest
    ) {
        Long managerId = (Long) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(
                managerOrderService.updateOrderStatusForManager(managerId, orderId, request)
        );
    }
}