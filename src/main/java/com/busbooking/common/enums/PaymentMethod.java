package com.busbooking.common.enums;

public enum PaymentMethod {
    CASH("Tiền mặt tại quầy / Khi lên xe"),
    BANK_TRANSFER("Chuyển khoản Ngân hàng (VietQR)"),
    E_WALLET("Ví điện tử (MoMo / VNPay / ZaloPay)");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
