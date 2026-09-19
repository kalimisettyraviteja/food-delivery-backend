package com.fooddelivery.orderservice.controller;

import com.fooddelivery.orderservice.dto.*;
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

    // ─── NEW: cancel order ───
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id,
            @Valid @RequestBody CancelOrderRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(orderService.cancelOrder(id, userId, request));
    }

    // ─── NEW: update delivery address ───
    @PatchMapping("/{id}/address")
    public ResponseEntity<OrderResponse> updateOrderAddress(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderAddressRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(orderService.updateOrderAddress(id, userId, request));
    }

    // ─── NEW: update receiver name + phone number ───
    @PatchMapping("/{id}/contact")
    public ResponseEntity<OrderResponse> updateOrderContact(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderContactRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(orderService.updateOrderContact(id, userId, request));
    }

    // ─── NEW: update cooking instructions ───
    @PatchMapping("/{id}/instructions")
    public ResponseEntity<OrderResponse> updateOrderInstructions(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderInstructionsRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ResponseEntity.ok(orderService.updateOrderInstructions(id, userId, request));
    }
}