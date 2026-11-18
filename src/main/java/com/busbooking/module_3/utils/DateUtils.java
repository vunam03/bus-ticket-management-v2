package com.busbooking.module_3.utils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Tiện ích định dạng ngày giờ tiếng Việt, tính thời lượng chuyến và phân loại khung giờ khởi hành (Module 3).
 */
public final class DateUtils {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_VN_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FULL_VN_FMT = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
    private static final Locale VI_LOCALE = new Locale("vi", "VN");
    private static final DateTimeFormatter DAY_OF_WEEK_FMT = DateTimeFormatter.ofPattern("EEEE", VI_LOCALE);

    private DateUtils() {
    }

    public static String formatTime(LocalDateTime dateTime) {
        return dateTime == null ? "--:--" : dateTime.format(TIME_FMT);
    }

    public static String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_VN_FMT);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(FULL_VN_FMT);
    }

    public static String formatVietnameseWeekday(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        String day = dateTime.format(DAY_OF_WEEK_FMT);
        if (day.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(day.charAt(0)) + day.substring(1);
    }

    public static long durationMinutes(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !end.isAfter(start)) {
            return 0L;
        }
        return Duration.between(start, end).toMinutes();
    }

    public static String formatDuration(LocalDateTime start, LocalDateTime end) {
        long minutes = durationMinutes(start, end);
        if (minutes <= 0) {
            return "Đang cập nhật";
        }
        long hours = minutes / 60;
        long remMinutes = minutes % 60;
        if (remMinutes == 0) {
            return hours + " giờ";
        }
        return hours + " giờ " + remMinutes + " phút";
    }

    public static String classifyTimeSlotCode(LocalDateTime departureTime) {
        if (departureTime == null) {
            return "MORNING";
        }
        int hour = departureTime.getHour();
        if (hour >= 5 && hour < 12) return "MORNING";
        if (hour >= 12 && hour < 18) return "AFTERNOON";
        if (hour >= 18) return "EVENING";
        return "NIGHT";
    }

    public static String getTimeSlotLabel(String timeSlotCode) {
        if (timeSlotCode == null) {
            return "Tất cả khung giờ";
        }
        return switch (timeSlotCode.toUpperCase(Locale.ROOT)) {
            case "MORNING" -> "Sáng (05:00 - 11:59)";
            case "AFTERNOON" -> "Chiều (12:00 - 17:59)";
            case "EVENING" -> "Tối (18:00 - 23:59)";
            case "NIGHT" -> "Đêm khuya (00:00 - 04:59)";
            default -> "Tất cả khung giờ";
        };
    }
}
