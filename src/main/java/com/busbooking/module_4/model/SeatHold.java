package com.busbooking.module_4.model;

import com.busbooking.module_2.model.User;
import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Thực thể lưu phiên giữ ghế tạm thời của người dùng (Module 4).
 */
@Entity
@Table(name = "seat_holds", indexes = {
        @Index(name = "idx_seathold_expiry", columnList = "expiresAt, active"),
        @Index(name = "idx_seathold_code", columnList = "holdCode")
})
public class SeatHold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String holdCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime heldAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean active = true;

    public SeatHold() {
    }

    public SeatHold(String holdCode, Seat seat, User user, LocalDateTime heldAt, LocalDateTime expiresAt) {
        this.holdCode = holdCode;
        this.seat = seat;
        this.user = user;
        this.heldAt = heldAt;
        this.expiresAt = expiresAt;
        this.active = true;
    }

    public boolean isExpired() {
        return isExpiredAt(LocalDateTime.now());
    }

    public boolean isExpiredAt(LocalDateTime referenceTime) {
        return expiresAt == null || referenceTime.isAfter(expiresAt);
    }

    public boolean isOwnedBy(User candidate) {
        return candidate != null && candidate.getId() != null
                && user != null && candidate.getId().equals(user.getId());
    }

    public boolean isOwnedByUsername(String username) {
        return username != null && !username.isBlank()
                && user != null && user.getUsername() != null
                && user.getUsername().equalsIgnoreCase(username.trim());
    }

    public long getRemainingSeconds() {
        if (!active || expiresAt == null) {
            return 0L;
        }
        return Math.max(0L, Duration.between(LocalDateTime.now(), expiresAt).getSeconds());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHoldCode() {
        return holdCode;
    }

    public void setHoldCode(String holdCode) {
        this.holdCode = holdCode;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getHeldAt() {
        return heldAt;
    }

    public void setHeldAt(LocalDateTime heldAt) {
        this.heldAt = heldAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
