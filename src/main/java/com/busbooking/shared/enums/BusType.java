package com.busbooking.shared.enums;

public enum BusType {
    LIMOUSINE_36("Limousine Phòng Đôi 36 chỗ", 36, 2),
    SLEEPER_36("Giường nằm VIP 36 chỗ", 36, 2),
    SLEEPER_40("Giường nằm tiêu chuẩn 40 chỗ", 40, 2),
    SEATER_45("Ghế ngồi cao cấp 45 chỗ", 45, 1);

    private final String displayName;
    private final int defaultSeats;
    private final int floors;

    BusType(String displayName, int defaultSeats, int floors) {
        this.displayName = displayName;
        this.defaultSeats = defaultSeats;
        this.floors = floors;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDefaultSeats() {
        return defaultSeats;
    }

    public int getTotalSeats() {
        return defaultSeats;
    }

    public int getFloors() {
        return floors;
    }
}
