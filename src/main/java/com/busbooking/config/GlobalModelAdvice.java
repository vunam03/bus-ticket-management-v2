package com.busbooking.config;

import com.busbooking.module_2.repository.UserAccountRepository;
import com.busbooking.common.enums.RoleName;
import com.busbooking.module_2.model.User;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
@Controller
public class GlobalModelAdvice {

    private final UserAccountRepository userRepository;

    public GlobalModelAdvice(UserAccountRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ModelAttribute
    public void addGlobalSecurityAttributes(Authentication auth, Model model) {
        boolean authenticated = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
        boolean isAdmin = false;
        String currentUsername = "";
        String currentFullName = "";

        if (authenticated) {
            currentUsername = auth.getName();
            isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals(RoleName.ROLE_ADMIN.name()));
            Optional<User> userOpt = userRepository.findByUsernameOrEmail(currentUsername);
            if (userOpt.isPresent()) {
                currentFullName = userOpt.get().getFullName();
            } else {
                currentFullName = currentUsername;
            }
        }

        model.addAttribute("isAuthenticated", authenticated);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("currentUsername", currentUsername);
        model.addAttribute("currentUserFullName", currentFullName);
    }

    @GetMapping("/auth/access-denied")
    public String accessDeniedPage(Model model) {
        model.addAttribute("statusCode", 403);
        model.addAttribute("errorTitle", "Truy cập bị từ chối (403 Forbidden)");
        model.addAttribute("errorMessage", "Tài khoản khách hàng không được phép truy cập vào trang Quản trị Admin.");
        return "common/error";
    }
}
