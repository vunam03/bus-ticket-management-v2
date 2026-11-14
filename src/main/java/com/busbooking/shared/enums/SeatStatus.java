package com.busbooking.shared.enums;

public enum SeatStatus {
    AVAILABLE("Còn trống"),
    HELD("Đang giữ chỗ"),
    BOOKED("Đã đặt");

    private final String displayName;

    SeatStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
