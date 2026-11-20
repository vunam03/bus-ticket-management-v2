package com.busbooking.module_2.security;

import com.busbooking.common.enums.RoleName;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tiện ích kiểm tra phiên đăng nhập, quyền truy cập và thông tin thiết bị người dùng (Module 2).
 */
@Component
public class SessionAuditHelper {

    public boolean isAuthenticated(Authentication auth) {
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }

    public boolean hasAdminRole(Authentication auth) {
        if (!isAuthenticated(auth)) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> RoleName.ROLE_ADMIN.name().equals(a.getAuthority()));
    }

    public String extractClientIp(HttpServletRequest request) {
        if (request == null) {
            return "127.0.0.1";
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : "127.0.0.1";
    }

    public String summarizeUserAgent(HttpServletRequest request) {
        if (request == null) {
            return "Trình duyệt Web";
        }
        String ua = request.getHeader("User-Agent");
        if (ua == null || ua.isBlank()) {
            return "Trình duyệt Web";
        }
        if (ua.contains("Edg")) return "Microsoft Edge";
        if (ua.contains("Chrome")) return "Google Chrome";
        if (ua.contains("Firefox")) return "Mozilla Firefox";
        if (ua.contains("Safari")) return "Apple Safari";
        return "Web Client";
    }

    public Map<String, Object> buildSessionAuditSummary(HttpServletRequest request, Authentication auth) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("authenticated", isAuthenticated(auth));
        info.put("username", isAuthenticated(auth) ? auth.getName() : "Khách");
        info.put("role", hasAdminRole(auth) ? "Quản trị viên (ADMIN)" : "Khách hàng (CUSTOMER)");
        info.put("clientIp", extractClientIp(request));
        info.put("browser", summarizeUserAgent(request));

        HttpSession session = request != null ? request.getSession(false) : null;
        if (session != null) {
            info.put("sessionIdShort", maskSessionId(session.getId()));
            info.put("createdAt", LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(session.getCreationTime()), ZoneId.systemDefault()));
            info.put("maxInactiveMinutes", session.getMaxInactiveInterval() / 60);
        } else {
            info.put("sessionIdShort", "N/A");
            info.put("maxInactiveMinutes", 30);
        }
        return info;
    }

    private String maskSessionId(String sessionId) {
        if (sessionId == null || sessionId.length() <= 8) {
            return "****";
        }
        return sessionId.substring(0, 4) + "****" + sessionId.substring(sessionId.length() - 4);
    }
}
