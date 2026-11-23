package com.coffeeshop.backend.dto;

import com.coffeeshop.backend.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(

    @NotNull(message = "Trang thai khong duoc de trong")
    OrderStatus status

) {}
