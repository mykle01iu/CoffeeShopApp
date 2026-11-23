package com.coffeeshop.backend.controller;

import com.coffeeshop.backend.dto.OrderStatusHistoryResponse;
import com.coffeeshop.backend.dto.UpdateOrderStatusRequest;
import com.coffeeshop.backend.service.OrderStatusService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderStatusController {

    private final OrderStatusService orderStatusService;

    public OrderStatusController(
            OrderStatusService orderStatusService) {
        this.orderStatusService = orderStatusService;
    }

    // Chi ADMIN duoc cap nhat trang thai don hang
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderStatusHistoryResponse> updateStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                orderStatusService.updateStatus(
                        orderId,
                        request,
                        authentication
                )
        );
    }

    // USER xem lich su don cua minh
    // ADMIN xem lich su cua tat ca don hang
    @GetMapping("/{orderId}/history")
    public ResponseEntity<List<OrderStatusHistoryResponse>> getHistory(
            @PathVariable Long orderId,
            Authentication authentication) {

        return ResponseEntity.ok(
                orderStatusService.getHistory(
                        orderId,
                        authentication
                )
        );
    }
}
