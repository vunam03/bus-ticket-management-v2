package com.busbooking.module_4.enums;

/**
 * Trạng thái vòng đời của một ghế trên chuyến xe (Module 4).
 */
public enum SeatStatus {
    AVAILABLE("Còn trống"),
    HELD("Đang giữ chỗ"),
    BOOKED("Đã đặt"),
    UNAVAILABLE("Không khả dụng");

    private final String displayName;

    SeatStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Chỉ ghế AVAILABLE mới có thể được chọn mới để giữ chỗ.
     */
    public boolean isSelectable() {
        return this == AVAILABLE;
    }

    /**
     * Ghế đang bị khóa, đã bán hoặc ngừng phục vụ thì khách khác không thể đặt.
     */
    public boolean isLockedOrSold() {
        return this == HELD || this == BOOKED || this == UNAVAILABLE;
    }

    /**
     * Kiểm tra chuyển đổi trạng thái hợp lệ theo vòng đời giữ ghế:
     * AVAILABLE -> HELD -> BOOKED (hoặc HELD -> AVAILABLE),
     * BOOKED -> AVAILABLE chỉ được phép qua nghiệp vụ hủy vé hợp lệ.
     */
    public boolean canTransitionTo(SeatStatus next) {
        if (next == null) {
            return false;
        }
        if (this == next) {
            return true;
        }
        return switch (this) {
            case AVAILABLE -> next == HELD || next == UNAVAILABLE;
            case HELD -> next == BOOKED || next == AVAILABLE;
            case BOOKED -> next == AVAILABLE;
            case UNAVAILABLE -> next == AVAILABLE;
        };
    }
}
