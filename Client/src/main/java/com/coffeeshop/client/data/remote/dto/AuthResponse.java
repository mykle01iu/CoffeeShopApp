package com.coffeeshop.client.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/** Backend trả thông tin đăng nhập ở cấp cao nhất, không lồng trong user. */
public final class AuthResponse {
    @SerializedName("token") public String token;
    @SerializedName("id") public Long id;
    @SerializedName("name") public String name;
    @SerializedName("email") public String email;
    @SerializedName("role") public String role;
}
