package com.busbooking.module_2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO yêu cầu đổi mật khẩu bảo mật cho tài khoản đang đăng nhập (Module 2).
 */
public class ChangePasswordRequest {

    @NotBlank(message = "Mật khẩu hiện tại không được để trống")
    private String currentPassword;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 6, max = 100, message = "Mật khẩu mới phải từ 6 ký tự trở lên")
    private String newPassword;

    @NotBlank(message = "Vui lòng xác nhận mật khẩu mới")
    private String confirmPassword;

    public ChangePasswordDto toChangePasswordDto() {
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setCurrentPassword(currentPassword);
        dto.setNewPassword(newPassword);
        dto.setConfirmPassword(confirmPassword);
        return dto;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
