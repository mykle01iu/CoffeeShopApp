package com.coffeeshop.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    @NotBlank(message = "Tên không được để trống")
    private String name;
    private String identifier;
    private boolean phone;
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    // Compatibility with older clients that post the email property.
    public void setEmail(String email) { this.identifier = email; this.phone = false; }
    public String getEmail() { return phone ? null : identifier; }
}
