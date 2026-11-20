package com.coffeeshop.backend.dto;

import lombok.Getter;

@Getter
public class AuthResponse {

    private String token;
    private String name;
    private String email;
    private String role;
    private String phone;

    public AuthResponse(String token, String name, String email, String role, String phone) {
        this.token = token;
        this.name = name;
        this.email = email;
        this.role = role;
        this.phone = phone;
    }
}
