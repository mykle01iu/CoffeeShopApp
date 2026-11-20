package com.coffeeshop.backend.dto;

import com.coffeeshop.backend.entity.Order;
import com.coffeeshop.backend.entity.OrderItem;
import com.coffeeshop.backend.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        LocalDateTime createdAt,
        OrderStatus status,
        BigDecimal totalAmount,
        List<Item> items) {

    public record Item(Long productId, String productName, Integer quantity,
                       BigDecimal unitPrice, BigDecimal lineTotal) { }

    public static OrderResponse from(Order order) {
        List<Item> items = order.getItems().stream().map(OrderResponse::from).toList();
        return new OrderResponse(order.getId(), order.getCreatedAt(), order.getStatus(),
                order.getTotalAmount(), items);
    }

    private static Item from(OrderItem item) {
        BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new Item(item.getProduct().getId(), item.getProduct().getName(),
                item.getQuantity(), item.getUnitPrice(), lineTotal);
    }
}
