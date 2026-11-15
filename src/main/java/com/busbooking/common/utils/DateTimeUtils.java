package com.busbooking.common.utils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtils {

    public static final DateTimeFormatter DATE_TIME_VN = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
    public static final DateTimeFormatter DATE_VN = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter TIME_VN = DateTimeFormatter.ofPattern("HH:mm");

    private DateTimeUtils() {
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_TIME_VN);
    }

    public static String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_VN);
    }

    public static String formatDuration(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || end.isBefore(start)) {
            return "N/A";
        }
        Duration d = Duration.between(start, end);
        long hours = d.toHours();
        long mins = d.toMinutesPart();
        if (mins == 0) {
            return hours + " giờ";
        }
        return hours + " giờ " + mins + " phút";
    }
}
