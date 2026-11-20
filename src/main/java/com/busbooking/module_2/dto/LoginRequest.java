package com.busbooking.module_2.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO thông tin đăng nhập phục vụ kiểm tra phiên và biểu mẫu đăng nhập (Module 2).
 */
public class LoginRequest {

    @NotBlank(message = "Vui lòng nhập tên đăng nhập hoặc email")
    private String identifier;

    @NotBlank(message = "Vui lòng nhập mật khẩu")
    private String password;

    private boolean rememberMe;

    public String getNormalizedIdentifier() {
        return identifier == null ? "" : identifier.trim().toLowerCase();
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
