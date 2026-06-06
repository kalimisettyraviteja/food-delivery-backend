package com.fooddelivery.orderservice.controller;

import com.fooddelivery.orderservice.dto.PlaceOrderRequest;
import com.fooddelivery.orderservice.dto.OrderResponse;
import com.fooddelivery.orderservice.dto.OrderSummaryResponse;
import com.fooddelivery.orderservice.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request,
            HttpServletRequest httpRequest) {

        Long userId   = (Long) httpRequest.getAttribute("userId");
        String email  = (String) httpRequest.getAttribute("email");

        return ResponseEntity.ok(orderService.placeOrder(userId, email, request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderSummaryResponse>> getMyOrders(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(orderService.getMyOrders(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(orderService.getOrderById(id, userId));
    }
}