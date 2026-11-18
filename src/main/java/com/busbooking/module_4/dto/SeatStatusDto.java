package com.busbooking.module_4.dto;

import com.busbooking.common.utils.CurrencyUtils;
import com.busbooking.module_4.enums.SeatStatus;
import com.busbooking.module_4.model.Seat;
import com.busbooking.module_4.model.SeatHold;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * DTO biểu diễn trạng thái từng ghế theo thời gian thực để render sơ đồ tầng và API JSON (Module 4).
 * Không tiết lộ mã giữ chỗ hoặc danh tính của người dùng khác.
 */
public class SeatStatusDto {

    private Long seatId;
    private String seatNumber;
    private Integer floorNumber;
    private Integer rowIndex;
    private Integer colIndex;
    private SeatStatus status;
    private String statusLabel;
    private BigDecimal price;
    private String formattedPrice;
    private String deckLabel;
    private boolean selectable;
    private boolean heldByCurrentUser;
    private boolean heldByOther;
    private String holdCode;
    private LocalDateTime holdExpiresAt;
    private long remainingSeconds;

    public static SeatStatusDto fromEntity(Seat seat, SeatHold activeHold, String currentUsername) {
        SeatStatusDto dto = new SeatStatusDto();
        dto.seatId = seat.getId();
        dto.seatNumber = seat.getSeatNumber();
        dto.floorNumber = seat.getFloorNumber();
        dto.rowIndex = seat.getRowIndex();
        dto.colIndex = seat.getColIndex();
        dto.status = seat.getStatus();
        dto.statusLabel = seat.getStatus() != null ? seat.getStatus().getDisplayName() : "";
        dto.price = seat.getPrice();
        dto.formattedPrice = CurrencyUtils.formatVnd(seat.getPrice());
        dto.deckLabel = (seat.getFloorNumber() != null && seat.getFloorNumber() == 2) ? "Tầng trên" : "Tầng dưới";

        boolean ownedByUser = false;
        if (seat.getStatus() == SeatStatus.HELD
                && activeHold != null
                && activeHold.isActive()
                && !activeHold.isExpired()
                && currentUsername != null
                && activeHold.getUser() != null
                && currentUsername.equalsIgnoreCase(activeHold.getUser().getUsername())) {
            ownedByUser = true;
            dto.holdCode = activeHold.getHoldCode();
            dto.holdExpiresAt = activeHold.getExpiresAt();
            dto.remainingSeconds = Math.max(0L, Duration.between(LocalDateTime.now(), activeHold.getExpiresAt()).getSeconds());
        }

        dto.heldByCurrentUser = ownedByUser;
        dto.heldByOther = seat.getStatus() == SeatStatus.HELD && !ownedByUser;
        dto.selectable = seat.getStatus() == SeatStatus.AVAILABLE || ownedByUser;
        if (ownedByUser) {
            dto.statusLabel = "Bạn đang giữ";
        } else if (dto.heldByOther) {
            dto.statusLabel = "Người khác đang giữ";
        }
        return dto;
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Integer getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(Integer floorNumber) {
        this.floorNumber = floorNumber;
    }

    public Integer getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(Integer rowIndex) {
        this.rowIndex = rowIndex;
    }

    public Integer getColIndex() {
        return colIndex;
    }

    public void setColIndex(Integer colIndex) {
        this.colIndex = colIndex;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getFormattedPrice() {
        return formattedPrice;
    }

    public void setFormattedPrice(String formattedPrice) {
        this.formattedPrice = formattedPrice;
    }

    public String getDeckLabel() {
        return deckLabel;
    }

    public void setDeckLabel(String deckLabel) {
        this.deckLabel = deckLabel;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public boolean isHeldByCurrentUser() {
        return heldByCurrentUser;
    }

    public void setHeldByCurrentUser(boolean heldByCurrentUser) {
        this.heldByCurrentUser = heldByCurrentUser;
    }

    public boolean isHeldByOther() {
        return heldByOther;
    }

    public void setHeldByOther(boolean heldByOther) {
        this.heldByOther = heldByOther;
    }

    public String getHoldCode() {
        return holdCode;
    }

    public void setHoldCode(String holdCode) {
        this.holdCode = holdCode;
    }

    public LocalDateTime getHoldExpiresAt() {
        return holdExpiresAt;
    }

    public void setHoldExpiresAt(LocalDateTime holdExpiresAt) {
        this.holdExpiresAt = holdExpiresAt;
    }

    public long getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(long remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }
}
