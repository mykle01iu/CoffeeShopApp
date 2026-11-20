package com.coffeeshop.client.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class ErrorResponseDto {
    @SerializedName("message")
    private String message;

    public ErrorResponseDto() { }

    public ErrorResponseDto(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
