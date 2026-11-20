package com.coffeeshop.client.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class AuthResponseDto {
    @SerializedName("token")
    private String token;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    @SerializedName("role")
    private String role;

    public AuthResponseDto() { }

    public AuthResponseDto(String token, String name, String email, String role) {
        this.token = token;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() { return phone; }

    public String getRole() {
        return role;
    }
}
