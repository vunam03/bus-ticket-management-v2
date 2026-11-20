package com.busbooking.module_2.controller;

import com.busbooking.module_2.dto.ChangePasswordDto;
import com.busbooking.module_2.dto.UserProfileResponse;
import com.busbooking.module_2.security.SessionAuditHelper;
import com.busbooking.module_2.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller bổ sung trang đổi mật khẩu chuyên biệt, nhật ký phiên đăng nhập và API hồ sơ người dùng (Module 2).
 */
@Controller
public class UserController {

    private final UserService userService;
    private final SessionAuditHelper sessionAuditHelper;

    public UserController(UserService userService, SessionAuditHelper sessionAuditHelper) {
        this.userService = userService;
        this.sessionAuditHelper = sessionAuditHelper;
    }

    @GetMapping({"/user/change-password", "/account/change-password", "/user/security"})
    public String showChangePasswordPage(Authentication auth, HttpServletRequest request, Model model) {
        if (!model.containsAttribute("passwordForm")) {
            model.addAttribute("passwordForm", new ChangePasswordDto());
        }
        model.addAttribute("profileSummary", userService.getProfileSummary(auth.getName()));
        model.addAttribute("sessionAudit", sessionAuditHelper.buildSessionAuditSummary(request, auth));
        return "module_2/change-password";
    }

    @GetMapping("/api/user/profile")
    @ResponseBody
    public ResponseEntity<UserProfileResponse> apiGetCurrentProfile(Authentication auth) {
        return ResponseEntity.ok(userService.getProfileSummary(auth.getName()));
    }
}
