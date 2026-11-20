package com.busbooking.module_2.security;

import com.busbooking.module_2.exception.AuthAccountException;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Bộ kiểm tra chính sách mật khẩu, chuẩn hóa dữ liệu định danh và chấm điểm độ mạnh mật khẩu (Module 2).
 */
@Component
public class SecurityPasswordPolicyValidator {

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 100;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{4,50}$");
    private static final Pattern VN_PHONE_PATTERN = Pattern.compile("^0\\d{9,10}$");

    public String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new AuthAccountException("INVALID_USERNAME", "Tên đăng nhập không được để trống.");
        }
        String normalized = username.trim().toLowerCase();
        if (!USERNAME_PATTERN.matcher(normalized).matches()) {
            throw new AuthAccountException("INVALID_USERNAME_FORMAT",
                    "Tên đăng nhập phải dài 4-50 ký tự và chỉ chứa chữ, số, dấu chấm, gạch dưới hoặc gạch ngang.");
        }
        return normalized;
    }

    public String normalizeEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new AuthAccountException("INVALID_EMAIL", "Địa chỉ email không đúng định dạng.");
        }
        return email.trim().toLowerCase();
    }

    public String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new AuthAccountException("INVALID_PHONE", "Số điện thoại không được để trống.");
        }
        String digits = phone.trim().replaceAll("[\\s.-]", "");
        if (!VN_PHONE_PATTERN.matcher(digits).matches()) {
            throw new AuthAccountException("INVALID_PHONE_FORMAT",
                    "Số điện thoại phải bắt đầu bằng số 0 và dài 10 hoặc 11 chữ số.");
        }
        return digits;
    }

    public void validatePasswordPair(String password, String confirmPassword) {
        validateRawPassword(password);
        if (confirmPassword == null || !password.equals(confirmPassword)) {
            throw AuthAccountException.passwordMismatch();
        }
    }

    public void validateRawPassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw AuthAccountException.weakPassword("Mật khẩu phải có ít nhất " + MIN_PASSWORD_LENGTH + " ký tự.");
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            throw AuthAccountException.weakPassword("Mật khẩu không được vượt quá " + MAX_PASSWORD_LENGTH + " ký tự.");
        }
        if (password.chars().anyMatch(Character::isWhitespace)) {
            throw AuthAccountException.weakPassword("Mật khẩu không được chứa khoảng trắng.");
        }
    }

    public Map<String, Object> evaluateStrength(String password) {
        int score = 0;
        if (password != null && !password.isEmpty()) {
            if (password.length() >= 6) score += 25;
            if (password.length() >= 10) score += 15;
            if (password.chars().anyMatch(Character::isUpperCase)) score += 15;
            if (password.chars().anyMatch(Character::isLowerCase)) score += 15;
            if (password.chars().anyMatch(Character::isDigit)) score += 15;
            if (password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch))) score += 15;
        }
        score = Math.min(100, score);

        String label;
        String badgeColor;
        if (score >= 85) {
            label = "Rất mạnh";
            badgeColor = "success";
        } else if (score >= 60) {
            label = "Mạnh";
            badgeColor = "primary";
        } else if (score >= 35) {
            label = "Trung bình";
            badgeColor = "warning";
        } else {
            label = "Yếu";
            badgeColor = "danger";
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("score", score);
        result.put("label", label);
        result.put("badgeColor", badgeColor);
        return result;
    }
}
