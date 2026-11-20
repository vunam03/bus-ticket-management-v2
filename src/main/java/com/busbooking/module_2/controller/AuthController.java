package com.busbooking.module_2.controller;

import com.busbooking.common.exception.BusinessException;
import com.busbooking.module_2.dto.ForgotPasswordDto;
import com.busbooking.module_2.dto.RegisterRequestDto;
import com.busbooking.module_2.security.SecurityPasswordPolicyValidator;
import com.busbooking.module_2.service.AuthAccountService;
import com.busbooking.module_2.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthAccountService authService;
    private final AuthService domainAuthService;
    private final SecurityPasswordPolicyValidator passwordPolicyValidator;

    public AuthController(AuthAccountService authService,
                          AuthService domainAuthService,
                          SecurityPasswordPolicyValidator passwordPolicyValidator) {
        this.authService = authService;
        this.domainAuthService = domainAuthService;
        this.passwordPolicyValidator = passwordPolicyValidator;
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "module_2/login";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new RegisterRequestDto());
        }
        return "module_2/register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("registerForm") RegisterRequestDto form,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "module_2/register";
        }
        try {
            authService.registerCustomer(form);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đăng ký tài khoản thành công! Vui lòng đăng nhập.");
            return "redirect:/auth/login";
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "module_2/register";
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPassword(Model model) {
        model.addAttribute("forgotForm", new ForgotPasswordDto());
        return "module_2/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email,
                                        Model model) {
        try {
            String token = authService.createPasswordResetToken(email);
            ForgotPasswordDto dto = new ForgotPasswordDto();
            dto.setEmail(email);
            dto.setToken(token);
            model.addAttribute("forgotForm", dto);
            model.addAttribute("generatedToken", token);
            model.addAttribute("successMessage",
                    "Mã xác thực khôi phục mật khẩu đã được tạo! (Chế độ mô phỏng: Mã khôi phục của bạn là " + token + ")");
        } catch (BusinessException ex) {
            ForgotPasswordDto dto = new ForgotPasswordDto();
            dto.setEmail(email);
            model.addAttribute("forgotForm", dto);
            model.addAttribute("errorMessage", ex.getMessage());
        }
        return "module_2/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam(required = false) String token, Model model) {
        ForgotPasswordDto dto = new ForgotPasswordDto();
        dto.setToken(token != null ? token.trim() : "");
        model.addAttribute("forgotForm", dto);
        model.addAttribute("tokenValid", token != null && domainAuthService.validateResetToken(token));
        return "module_2/reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("newPassword") String newPassword,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {
        try {
            authService.resetPasswordWithToken(token, newPassword, confirmPassword);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đặt lại mật khẩu mới thành công! Bạn có thể đăng nhập ngay.");
            return "redirect:/auth/login";
        } catch (BusinessException ex) {
            ForgotPasswordDto dto = new ForgotPasswordDto();
            dto.setToken(token);
            model.addAttribute("forgotForm", dto);
            model.addAttribute("generatedToken", token);
            model.addAttribute("errorMessage", ex.getMessage());
            return "module_2/forgot-password";
        }
    }

    @GetMapping("/api/check-availability")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkAccountAvailability(@RequestParam(required = false) String username,
                                                                        @RequestParam(required = false) String email) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (username != null) {
            result.put("usernameAvailable", domainAuthService.isUsernameAvailable(username));
        }
        if (email != null) {
            result.put("emailAvailable", domainAuthService.isEmailAvailable(email));
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/password-strength")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkPasswordStrength(@RequestBody Map<String, String> payload) {
        String password = payload != null ? payload.getOrDefault("password", "") : "";
        return ResponseEntity.ok(passwordPolicyValidator.evaluateStrength(password));
    }
}
