package com.busbooking.common.enums;

public enum TripStatus {
    SCHEDULED("Sắp khởi hành"),
    IN_PROGRESS("Đang chạy"),
    COMPLETED("Đã hoàn thành"),
    CANCELLED("Đã hủy chuyến");

    private final String displayName;

    TripStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
