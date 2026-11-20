package com.busbooking.module_2.service;

import com.busbooking.common.enums.RoleName;
import com.busbooking.module_2.dto.LoginRequest;
import com.busbooking.module_2.dto.RegisterRequest;
import com.busbooking.module_2.dto.UserProfileResponse;
import com.busbooking.module_2.exception.AuthAccountException;
import com.busbooking.module_2.model.Role;
import com.busbooking.module_2.model.User;
import com.busbooking.module_2.repository.RoleRepository;
import com.busbooking.module_2.repository.UserRepository;
import com.busbooking.module_2.security.SecurityPasswordPolicyValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service xử lý đăng ký, xác thực thông tin đăng nhập và khôi phục mật khẩu (Module 2).
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityPasswordPolicyValidator policyValidator;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       SecurityPasswordPolicyValidator policyValidator) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.policyValidator = policyValidator;
    }

    @Transactional
    public UserProfileResponse registerNewCustomer(RegisterRequest request) {
        String username = policyValidator.normalizeUsername(request.getUsername());
        String email = policyValidator.normalizeEmail(request.getEmail());
        String phone = policyValidator.normalizePhone(request.getPhone());
        policyValidator.validatePasswordPair(request.getPassword(), request.getConfirmPassword());

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw AuthAccountException.usernameTaken(username);
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw AuthAccountException.emailTaken(email);
        }

        Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_CUSTOMER, "Khách hàng")));

        User user = new User(
                username,
                email,
                request.getFullName().trim(),
                phone,
                passwordEncoder.encode(request.getPassword()),
                customerRole
        );
        user.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);
        return UserProfileResponse.fromEntity(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse verifyCredentials(LoginRequest request) {
        String identifier = request.getNormalizedIdentifier();
        User user = userRepository.findByUsernameOrEmail(identifier)
                .orElseThrow(() -> new AuthAccountException("INVALID_CREDENTIALS", "Tên đăng nhập hoặc mật khẩu không chính xác."));
        if (!user.isEnabled()) {
            throw AuthAccountException.accountDisabled(user.getUsername());
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthAccountException("INVALID_CREDENTIALS", "Tên đăng nhập hoặc mật khẩu không chính xác.");
        }
        return UserProfileResponse.fromEntity(user);
    }

    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        return !userRepository.existsByUsernameIgnoreCase(username.trim());
    }

    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return !userRepository.existsByEmailIgnoreCase(email.trim());
    }

    @Transactional
    public String issueResetToken(String email) {
        String normalizedEmail = policyValidator.normalizeEmail(email);
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new AuthAccountException("EMAIL_NOT_FOUND", "Không tìm thấy tài khoản nào đăng ký với email này."));
        String token = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        user.setResetPasswordToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30));
        userRepository.save(user);
        return token;
    }

    @Transactional(readOnly = true)
    public boolean validateResetToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return userRepository.findByResetPasswordToken(token.trim().toUpperCase())
                .map(User::hasActiveResetToken)
                .orElse(false);
    }

    @Transactional
    public void completePasswordReset(String token, String newPassword, String confirmPassword) {
        if (token == null || token.isBlank()) {
            throw AuthAccountException.invalidResetToken("Mã khôi phục không hợp lệ.");
        }
        policyValidator.validatePasswordPair(newPassword, confirmPassword);
        User user = userRepository.findByResetPasswordToken(token.trim().toUpperCase())
                .orElseThrow(() -> AuthAccountException.invalidResetToken("Mã khôi phục không tồn tại hoặc đã được sử dụng."));
        if (!user.hasActiveResetToken()) {
            throw AuthAccountException.invalidResetToken("Mã khôi phục đã hết hạn hiệu lực (30 phút).");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }
}
