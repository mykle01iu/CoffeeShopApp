package com.coffeeshop.backend.dto;

import java.time.LocalDateTime;

public record UserProfileResponse(
        Long id,
        String name,
        String email,
        String role,
        LocalDateTime createdAt
) {
}

