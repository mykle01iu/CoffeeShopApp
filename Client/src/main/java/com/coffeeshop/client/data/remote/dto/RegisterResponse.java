package com.coffeeshop.client.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/** Đăng ký hiện trả hồ sơ; ứng dụng gọi login ngay sau đó để nhận token. */
public final class RegisterResponse {
    @SerializedName("id") public Long id;
    @SerializedName("name") public String name;
    @SerializedName("email") public String email;
    @SerializedName("role") public String role;
}
