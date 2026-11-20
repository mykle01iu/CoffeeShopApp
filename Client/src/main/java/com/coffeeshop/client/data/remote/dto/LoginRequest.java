package com.coffeeshop.client.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public final class LoginRequest {
    @SerializedName("email") public final String email;
    @SerializedName("password") public final String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
