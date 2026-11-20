package com.coffeeshop.client.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class RegisterRequestDto {
    @SerializedName("name")
    private final String name;

    @SerializedName("identifier")
    private final String identifier;

    @SerializedName("phone")
    private final boolean phone;

    @SerializedName("password")
    private final String password;

    public RegisterRequestDto(String name, String identifier, String password, boolean phone) {
        this.name = name;
        this.identifier = identifier;
        this.phone = phone;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return identifier;
    }

    public String getPassword() {
        return password;
    }
}
