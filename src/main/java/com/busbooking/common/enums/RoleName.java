package com.busbooking.common.enums;

public enum RoleName {
    ROLE_ADMIN("Quản trị viên"),
    ROLE_CUSTOMER("Khách hàng");

    private final String displayName;

    RoleName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
