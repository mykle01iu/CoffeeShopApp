package com.coffeeshop.client.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public final class CreateOrderRequestDto {
    @SerializedName("items")
    public final List<Item> items;

    public CreateOrderRequestDto(List<Item> items) {
        this.items = items;
    }

    public static final class Item {
        @SerializedName("productId")
        public final Long productId;
        @SerializedName("quantity")
        public final int quantity;

        public Item(Long productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
    }
}
