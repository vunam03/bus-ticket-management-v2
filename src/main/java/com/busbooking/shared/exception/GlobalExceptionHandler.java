package com.busbooking.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public Object handleNotFound(ResourceNotFoundException ex, HttpServletRequest request, Model model) {
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMap(ex.getErrorCode(), ex.getMessage()));
        }
        model.addAttribute("statusCode", 404);
        model.addAttribute("errorTitle", "Không tìm thấy dữ liệu");
        model.addAttribute("errorMessage", ex.getMessage());
        return "shared/error";
    }

    @ExceptionHandler(BusinessException.class)
    public Object handleBusinessException(BusinessException ex, HttpServletRequest request, Model model) {
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap(ex.getErrorCode(), ex.getMessage()));
        }
        model.addAttribute("statusCode", 400);
        model.addAttribute("errorTitle", "Thao tác không hợp lệ");
        model.addAttribute("errorMessage", ex.getMessage());
        return "shared/error";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDenied(AccessDeniedException ex, HttpServletRequest request, Model model) {
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorMap("FORBIDDEN", "Bạn không có quyền truy cập chức năng này."));
        }
        model.addAttribute("statusCode", 403);
        model.addAttribute("errorTitle", "Từ chối truy cập (403 Forbidden)");
        model.addAttribute("errorMessage", "Tài khoản của bạn không có quyền truy cập vào khu vực Quản trị Admin.");
        return "shared/error";
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String accept = request.getHeader("Accept");
        return uri.contains("/api/") || (accept != null && accept.contains("application/json"));
    }

    private Map<String, Object> errorMap(String code, String message) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("success", false);
        map.put("errorCode", code);
        map.put("message", message);
        return map;
    }
}
