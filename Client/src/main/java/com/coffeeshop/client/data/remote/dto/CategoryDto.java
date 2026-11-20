package com.coffeeshop.client.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class CategoryDto {
    @SerializedName("id")
    public Long id;

    @SerializedName("name")
    public String name;

    @SerializedName("code")
    public String code;

    @SerializedName("description")
    public String description;

    @SerializedName("available")
    public Boolean available;

    public CategoryDto() { }

    public CategoryDto(Long id, String name, String code, String description, Boolean available) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
        this.available = available;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getAvailable() {
        return available;
    }
}
