package com.busbooking.module_2.exception;

import com.busbooking.common.exception.BusinessException;

/**
 * Ngoại lệ xác thực, đăng ký và quản lý tài khoản người dùng (Module 2).
 */
public class AuthAccountException extends BusinessException {

    public AuthAccountException(String errorCode, String message) {
        super(errorCode, message);
    }

    public static AuthAccountException usernameTaken(String username) {
        return new AuthAccountException("USERNAME_TAKEN", "Tên đăng nhập '" + username + "' đã được sử dụng.");
    }

    public static AuthAccountException emailTaken(String email) {
        return new AuthAccountException("EMAIL_TAKEN", "Email '" + email + "' đã được đăng ký trước đó.");
    }

    public static AuthAccountException passwordMismatch() {
        return new AuthAccountException("PASSWORD_MISMATCH", "Mật khẩu và xác nhận mật khẩu không trùng khớp.");
    }

    public static AuthAccountException weakPassword(String reason) {
        return new AuthAccountException("WEAK_PASSWORD", reason);
    }

    public static AuthAccountException invalidCurrentPassword() {
        return new AuthAccountException("INVALID_CURRENT_PASSWORD", "Mật khẩu hiện tại không chính xác.");
    }

    public static AuthAccountException invalidResetToken(String reason) {
        return new AuthAccountException("INVALID_RESET_TOKEN", reason);
    }

    public static AuthAccountException accountDisabled(String username) {
        return new AuthAccountException("ACCOUNT_DISABLED", "Tài khoản '" + username + "' hiện đang bị tạm khóa.");
    }
}
