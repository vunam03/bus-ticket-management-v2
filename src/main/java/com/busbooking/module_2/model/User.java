package com.busbooking.module_2.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_username", columnList = "username", unique = true),
        @Index(name = "idx_user_email", columnList = "email", unique = true)
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String address;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(length = 100)
    private String resetPasswordToken;

    private LocalDateTime resetTokenExpiry;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public User() {
    }

    public User(String username, String email, String fullName, String phone, String passwordHash, Role role) {
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.role = role;
        this.enabled = true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getResetPasswordToken() {
        return resetPasswordToken;
    }

    public void setResetPasswordToken(String resetPasswordToken) {
        this.resetPasswordToken = resetPasswordToken;
    }

    public LocalDateTime getResetTokenExpiry() {
        return resetTokenExpiry;
    }

    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) {
        this.resetTokenExpiry = resetTokenExpiry;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Transient
    public boolean isAdminAccount() {
        return role != null && role.getName() == com.busbooking.common.enums.RoleName.ROLE_ADMIN;
    }

    @Transient
    public String getRoleDisplayName() {
        return role != null && role.getName() != null ? role.getName().getDisplayName() : "Khách hàng";
    }

    @Transient
    public boolean hasActiveResetToken() {
        return resetPasswordToken != null
                && !resetPasswordToken.isBlank()
                && resetTokenExpiry != null
                && LocalDateTime.now().isBefore(resetTokenExpiry);
    }

    @Transient
    public String getMaskedPhone() {
        if (phone == null || phone.length() < 6) {
            return phone != null ? phone : "Chưa cập nhật";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 3);
    }

    @Transient
    public String getInitials() {
        if (fullName == null || fullName.isBlank()) {
            return "U";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
