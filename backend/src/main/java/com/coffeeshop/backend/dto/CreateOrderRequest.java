
package com.coffeeshop.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record CreateOrderRequest(

    @NotEmpty(message = "Don hang phai co san pham")
    @Size(max = 50, message = "Toi da 50 loai san pham")
    List<@NotNull @Valid Item> items

) {
    public record Item(

        @NotNull(message = "Thieu ma san pham")
        @Positive(message = "Ma san pham khong hop le")
        Long productId,

        @NotNull(message = "Thieu so luong")
        @Min(value = 1, message = "So luong phai >= 1")
        @Max(value = 100, message = "So luong toi da 100")
        Integer quantity

    ) {}
}
