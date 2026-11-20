package com.coffeeshop.client.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class ProductDto {
    @SerializedName("id")
    public Long id;

    @SerializedName("name")
    public String name;

    @SerializedName("description")
    public String description;

    @SerializedName("price")
    public Double price;

    @SerializedName("imageUrl")
    public String imageUrl;

    @SerializedName("category")
    public String category;

    @SerializedName("available")
    public Boolean available;

    public ProductDto() { }

    public ProductDto(Long id, String name, String description, Double price, String imageUrl, String category, Boolean available) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = category;
        this.available = available;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public Boolean getAvailable() {
        return available;
    }
}
