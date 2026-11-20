package com.busbooking.module_2.controller;

import com.busbooking.module_2.dto.ChangePasswordDto;
import com.busbooking.module_2.dto.ProfileUpdateDto;
import com.busbooking.module_2.service.AuthAccountService;
import com.busbooking.common.exception.BusinessException;
import com.busbooking.module_2.model.User;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping({"/user", "/account"})
public class UserProfileController {

    private final AuthAccountService authService;

    public UserProfileController(AuthAccountService authService) {
        this.authService = authService;
    }

    @GetMapping("/profile")
    public String showProfile(Authentication auth, Model model) {
        User user = authService.getByUsername(auth.getName());
        if (!model.containsAttribute("profileForm")) {
            ProfileUpdateDto profileForm = new ProfileUpdateDto();
            profileForm.setFullName(user.getFullName());
            profileForm.setEmail(user.getEmail());
            profileForm.setPhone(user.getPhone());
            profileForm.setAddress(user.getAddress());
            model.addAttribute("profileForm", profileForm);
        }
        if (!model.containsAttribute("passwordForm")) {
            model.addAttribute("passwordForm", new ChangePasswordDto());
        }
        model.addAttribute("userEntity", user);
        return "module_2/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(Authentication auth,
                                @Valid @ModelAttribute("profileForm") ProfileUpdateDto profileForm,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            User user = authService.getByUsername(auth.getName());
            model.addAttribute("userEntity", user);
            model.addAttribute("passwordForm", new ChangePasswordDto());
            return "module_2/profile";
        }
        try {
            authService.updateProfile(auth.getName(), profileForm);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin cá nhân thành công!");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/user/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(Authentication auth,
                                 @Valid @ModelAttribute("passwordForm") ChangePasswordDto passwordForm,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng kiểm tra lại mật khẩu mới (tối thiểu 6 ký tự).");
            return "redirect:/user/profile";
        }
        try {
            authService.changePassword(auth.getName(), passwordForm);
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/user/profile";
    }
}
