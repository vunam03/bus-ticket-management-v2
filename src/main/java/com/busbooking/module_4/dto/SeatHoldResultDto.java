package com.busbooking.module_4.dto;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO kết quả thao tác giữ ghế hoặc gia hạn giữ ghế (Module 4).
 */
public class SeatHoldResultDto {

    private String holdCode;
    private Long tripId;
    private List<Long> seatIds;
    private List<String> seatNumbers;
    private BigDecimal estimatedTotal;
    private LocalDateTime heldAt;
    private LocalDateTime expiresAt;
    private LocalDateTime maxExpiresAt;
    private long remainingSeconds;
    private boolean canExtend;
    private String message;

    public SeatHoldResultDto() {
    }

    public SeatHoldResultDto(String holdCode, Long tripId, List<Long> seatIds,
                             List<String> seatNumbers, BigDecimal estimatedTotal, LocalDateTime expiresAt) {
        this.holdCode = holdCode;
        this.tripId = tripId;
        this.seatIds = seatIds;
        this.seatNumbers = seatNumbers;
        this.estimatedTotal = estimatedTotal;
        this.expiresAt = expiresAt;
        this.remainingSeconds = expiresAt != null
                ? Math.max(0L, Duration.between(LocalDateTime.now(), expiresAt).getSeconds())
                : 0L;
    }

    public SeatHoldResultDto(String holdCode, Long tripId, List<Long> seatIds, List<String> seatNumbers,
                             BigDecimal estimatedTotal, LocalDateTime heldAt, LocalDateTime expiresAt,
                             LocalDateTime maxExpiresAt, boolean canExtend, String message) {
        this.holdCode = holdCode;
        this.tripId = tripId;
        this.seatIds = seatIds;
        this.seatNumbers = seatNumbers;
        this.estimatedTotal = estimatedTotal;
        this.heldAt = heldAt;
        this.expiresAt = expiresAt;
        this.maxExpiresAt = maxExpiresAt;
        this.remainingSeconds = expiresAt != null
                ? Math.max(0L, Duration.between(LocalDateTime.now(), expiresAt).getSeconds())
                : 0L;
        this.canExtend = canExtend;
        this.message = message;
    }

    public String getHoldCode() {
        return holdCode;
    }

    public void setHoldCode(String holdCode) {
        this.holdCode = holdCode;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public List<Long> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<Long> seatIds) {
        this.seatIds = seatIds;
    }

    public List<String> getSeatNumbers() {
        return seatNumbers;
    }

    public void setSeatNumbers(List<String> seatNumbers) {
        this.seatNumbers = seatNumbers;
    }

    public BigDecimal getEstimatedTotal() {
        return estimatedTotal;
    }

    public void setEstimatedTotal(BigDecimal estimatedTotal) {
        this.estimatedTotal = estimatedTotal;
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

    public LocalDateTime getMaxExpiresAt() {
        return maxExpiresAt;
    }

    public void setMaxExpiresAt(LocalDateTime maxExpiresAt) {
        this.maxExpiresAt = maxExpiresAt;
    }

    public long getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(long remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }

    public boolean isCanExtend() {
        return canExtend;
    }

    public void setCanExtend(boolean canExtend) {
        this.canExtend = canExtend;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
