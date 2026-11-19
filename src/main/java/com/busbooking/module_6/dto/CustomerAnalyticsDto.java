package com.busbooking.module_6.dto;

import com.busbooking.common.utils.CurrencyUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CustomerAnalyticsDto {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private boolean enabled;
    private long totalTicketsBooked;
    private long paidTicketsCount;
    private BigDecimal totalSpent = BigDecimal.ZERO;
    private LocalDateTime registeredAt;

    public CustomerAnalyticsDto() {
    }

    public CustomerAnalyticsDto(Long id, String username, String fullName, String email, String phone,
                                boolean enabled, long totalTicketsBooked, long paidTicketsCount,
                                BigDecimal totalSpent, LocalDateTime registeredAt) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.enabled = enabled;
        this.totalTicketsBooked = totalTicketsBooked;
        this.paidTicketsCount = paidTicketsCount;
        this.totalSpent = totalSpent != null ? totalSpent : BigDecimal.ZERO;
        this.registeredAt = registeredAt;
    }

    public String getFormattedTotalSpent() {
        return CurrencyUtils.formatVnd(totalSpent);
    }

    public String getCustomerTier() {
        if (totalSpent.compareTo(new BigDecimal("2000000")) >= 0) {
            return "Kim Cương";
        }
        if (totalSpent.compareTo(new BigDecimal("800000")) >= 0) {
            return "Vàng";
        }
        if (totalSpent.compareTo(new BigDecimal("300000")) >= 0) {
            return "Bạc";
        }
        return "Thành viên";
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getTotalTicketsBooked() {
        return totalTicketsBooked;
    }

    public void setTotalTicketsBooked(long totalTicketsBooked) {
        this.totalTicketsBooked = totalTicketsBooked;
    }

    public long getPaidTicketsCount() {
        return paidTicketsCount;
    }

    public void setPaidTicketsCount(long paidTicketsCount) {
        this.paidTicketsCount = paidTicketsCount;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public Long getUserId() {
        return id;
    }

    public long getCancelledTickets() {
        return Math.max(0L, totalTicketsBooked - paidTicketsCount);
    }

    public LocalDateTime getLastBookingTime() {
        return registeredAt;
    }
}
