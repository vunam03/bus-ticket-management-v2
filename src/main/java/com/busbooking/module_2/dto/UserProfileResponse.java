package com.busbooking.module_2.dto;

import com.busbooking.module_2.model.User;

import java.time.LocalDateTime;

/**
 * DTO phản hồi hồ sơ tài khoản an toàn (không chứa mật khẩu mã hóa hay token bí mật).
 */
public class UserProfileResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String initials;
    private String phone;
    private String maskedPhone;
    private String address;
    private String roleCode;
    private String roleDisplayName;
    private boolean adminAccount;
    private boolean enabled;
    private boolean resetTokenActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserProfileResponse fromEntity(User user) {
        UserProfileResponse res = new UserProfileResponse();
        res.id = user.getId();
        res.username = user.getUsername();
        res.email = user.getEmail();
        res.fullName = user.getFullName();
        res.initials = user.getInitials();
        res.phone = user.getPhone();
        res.maskedPhone = user.getMaskedPhone();
        res.address = user.getAddress();
        if (user.getRole() != null && user.getRole().getName() != null) {
            res.roleCode = user.getRole().getName().name();
            res.roleDisplayName = user.getRole().getName().getDisplayName();
        }
        res.adminAccount = user.isAdminAccount();
        res.enabled = user.isEnabled();
        res.resetTokenActive = user.hasActiveResetToken();
        res.createdAt = user.getCreatedAt();
        res.updatedAt = user.getUpdatedAt();
        return res;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getInitials() {
        return initials;
    }

    public String getPhone() {
        return phone;
    }

    public String getMaskedPhone() {
        return maskedPhone;
    }

    public String getAddress() {
        return address;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public String getRoleDisplayName() {
        return roleDisplayName;
    }

    public boolean isAdminAccount() {
        return adminAccount;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isResetTokenActive() {
        return resetTokenActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
