package com.coffeeshop.client.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public final class RegisterRequest {
    @SerializedName("name") public final String name;
    @SerializedName("email") public final String email;
    @SerializedName("password") public final String password;

    public RegisterRequest(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
