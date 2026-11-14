package com.busbooking.shared.utils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class CodeGeneratorUtils {

    private static final String ALPHANUM = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter COMPACT_DATE = DateTimeFormatter.ofPattern("yyMMdd");

    private CodeGeneratorUtils() {
    }

    public static String generateTripCode() {
        return "TRP-" + LocalDateTime.now().format(COMPACT_DATE) + "-" + randomChars(4);
    }

    public static String generateTicketCode() {
        return "VEX-" + LocalDateTime.now().format(COMPACT_DATE) + "-" + randomChars(5);
    }

    public static String generateHoldCode() {
        return "HLD-" + randomChars(8);
    }

    public static String generateTransactionCode() {
        return "TXN-" + LocalDateTime.now().format(COMPACT_DATE) + "-" + randomChars(6);
    }

    private static String randomChars(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUM.charAt(RANDOM.nextInt(ALPHANUM.length())));
        }
        return sb.toString();
    }
}
