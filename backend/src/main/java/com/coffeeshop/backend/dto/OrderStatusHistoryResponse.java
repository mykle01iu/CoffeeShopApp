package com.coffeeshop.backend.dto;

import java.time.LocalDateTime;

public record OrderStatusHistoryResponse(

    Long id,
    Long orderId,
    String fromStatus,
    String toStatus,
    String changedBy,
    LocalDateTime changedAt

) {}
