package com.coffeeshop.client.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class LoginRequestDto {
    @SerializedName("identifier")
    private final String identifier;

    @SerializedName("password")
    private final String password;

    public LoginRequestDto(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }

    public String getEmail() {
        return identifier;
    }

    public String getPassword() {
        return password;
    }
}
