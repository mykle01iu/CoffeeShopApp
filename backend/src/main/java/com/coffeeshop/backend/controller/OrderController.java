
package com.coffeeshop.backend.controller;

import com.coffeeshop.backend.dto.CreateOrderRequest;
import com.coffeeshop.backend.dto.OrderResponse;
import com.coffeeshop.backend.service.OrderService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 1. Tao don hang
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {

        OrderResponse response = orderService.createOrder(
                authentication.getName(),
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // 2. Lay don hang cua user dang dang nhap
    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            Authentication authentication) {

        List<OrderResponse> orders =
                orderService.getMyOrders(
                        authentication.getName()
                );

        return ResponseEntity.ok(orders);
    }

    // 3. Admin lay tat ca don hang
    @GetMapping("/admin")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {

        return ResponseEntity.ok(
                orderService.getAllOrders()
        );
    }
}
