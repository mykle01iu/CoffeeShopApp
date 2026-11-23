package com.coffeeshop.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(

        @NotBlank(message = "Ten khong duoc de trong")
        @Size(
                max = 100,
                message = "Ten khong duoc vuot qua 100 ky tu"
        )
        String name

) {
}
