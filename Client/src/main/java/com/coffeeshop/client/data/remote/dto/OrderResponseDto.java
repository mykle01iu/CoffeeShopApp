package com.coffeeshop.client.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.List;

public final class OrderResponseDto {
    @SerializedName("id") public Long id;
    @SerializedName("createdAt") public String createdAt;
    @SerializedName("status") public String status;
    @SerializedName("totalAmount") public BigDecimal totalAmount;
    @SerializedName("items") public List<Item> items;

    public static final class Item {
        @SerializedName("productId") public Long productId;
        @SerializedName("productName") public String productName;
        @SerializedName("quantity") public Integer quantity;
        @SerializedName("unitPrice") public BigDecimal unitPrice;
        @SerializedName("lineTotal") public BigDecimal lineTotal;
    }
}
