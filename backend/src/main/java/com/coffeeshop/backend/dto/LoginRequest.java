package com.coffeeshop.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "Email hoặc số điện thoại không được để trống")
    private String identifier;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    public void setEmail(String email) { this.identifier = email; }
    public String getEmail() { return identifier; }
}
