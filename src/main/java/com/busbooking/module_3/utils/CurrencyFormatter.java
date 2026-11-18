package com.busbooking.module_3.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Tiện ích định dạng giá vé, phân khúc giá và quy đổi tiền tệ hiển thị cho khách hàng (Module 3).
 */
public final class CurrencyFormatter {

    private static final BigDecimal ECONOMY_THRESHOLD = new BigDecimal("250000");
    private static final BigDecimal STANDARD_THRESHOLD = new BigDecimal("450000");

    private CurrencyFormatter() {
    }

    public static String formatVnd(BigDecimal amount) {
        if (amount == null) {
            return "0 đ";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        DecimalFormat formatter = new DecimalFormat("#,##0", symbols);
        return formatter.format(amount) + " đ";
    }

    public static String formatCompactThousand(BigDecimal amount) {
        if (amount == null) {
            return "0k";
        }
        BigDecimal thousands = amount.divide(new BigDecimal("1000"), 0, RoundingMode.HALF_UP);
        return thousands.toPlainString() + "k";
    }

    public static String classifyPriceTier(BigDecimal ticketPrice) {
        if (ticketPrice == null) {
            return "Phổ thông";
        }
        if (ticketPrice.compareTo(ECONOMY_THRESHOLD) <= 0) {
            return "Giá tiết kiệm";
        }
        if (ticketPrice.compareTo(STANDARD_THRESHOLD) <= 0) {
            return "Phổ thông";
        }
        return "Hạng thương gia";
    }

    public static BigDecimal estimateRoundTripTotal(BigDecimal oneWayPrice, int seatCount) {
        if (oneWayPrice == null || seatCount <= 0) {
            return BigDecimal.ZERO;
        }
        return oneWayPrice.multiply(BigDecimal.valueOf(seatCount * 2L));
    }
}
