
package com.coffeeshop.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(

    Long id,
    Long userId,
    String customerName,
    String customerEmail,
    String status,
    BigDecimal totalAmount,
    LocalDateTime createdAt,
    List<ItemResponse> items

) {

    public record ItemResponse(
        Long productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineTotal
    ) {}

}
