package com.busbooking.module_4.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SeatHoldResultDto {

    private String holdCode;
    private Long tripId;
    private List<Long> seatIds;
    private List<String> seatNumbers;
    private BigDecimal estimatedTotal;
    private LocalDateTime expiresAt;

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

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
