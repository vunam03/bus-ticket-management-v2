package com.busbooking.module_2.service;

import com.busbooking.module_2.dto.ChangePasswordDto;
import com.busbooking.module_2.dto.ProfileUpdateDto;
import com.busbooking.module_2.dto.RegisterRequestDto;
import com.busbooking.module_2.repository.RoleRepository;
import com.busbooking.module_2.repository.UserAccountRepository;
import com.busbooking.common.enums.RoleName;
import com.busbooking.common.exception.BusinessException;
import com.busbooking.common.exception.ResourceNotFoundException;
import com.busbooking.module_2.model.Role;
import com.busbooking.module_2.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthAccountService {

    private final UserAccountRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthAccountService(UserAccountRepository userRepository,
                              RoleRepository roleRepository,
                              PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public User getByUsername(String username) {
        return userRepository.findByUsernameOrEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + username));
    }

    @Transactional
    public User registerCustomer(RegisterRequestDto dto) {
        String username = dto.getUsername().trim().toLowerCase();
        String email = dto.getEmail().trim().toLowerCase();

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("PASSWORD_MISMATCH", "Mật khẩu xác nhận không khớp.");
        }
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new BusinessException("USERNAME_TAKEN", "Tên đăng nhập '" + username + "' đã được sử dụng.");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("EMAIL_TAKEN", "Email '" + email + "' đã được đăng ký.");
        }

        Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_CUSTOMER, "Khách hàng")));

        User user = new User(
                username,
                email,
                dto.getFullName().trim(),
                dto.getPhone().trim(),
                passwordEncoder.encode(dto.getPassword()),
                customerRole
        );
        return userRepository.save(user);
    }

    @Transactional
    public User updateProfile(String username, ProfileUpdateDto dto) {
        User user = getByUsername(username);
        String newEmail = dto.getEmail().trim().toLowerCase();

        userRepository.findByEmailIgnoreCase(newEmail).ifPresent(existing -> {
            if (!existing.getId().equals(user.getId())) {
                throw new BusinessException("EMAIL_TAKEN", "Email này đã được sử dụng bởi tài khoản khác.");
            }
        });

        user.setFullName(dto.getFullName().trim());
        user.setEmail(newEmail);
        user.setPhone(dto.getPhone().trim());
        user.setAddress(dto.getAddress() != null ? dto.getAddress().trim() : null);
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(String username, ChangePasswordDto dto) {
        User user = getByUsername(username);
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException("INVALID_CURRENT_PASSWORD", "Mật khẩu hiện tại không chính xác.");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("PASSWORD_MISMATCH", "Mật khẩu mới và xác nhận mật khẩu không khớp.");
        }
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public String createPasswordResetToken(String email) {
        User user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new BusinessException("EMAIL_NOT_FOUND", "Không tìm thấy tài khoản nào đăng ký với email này."));
        String token = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        user.setResetPasswordToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30));
        userRepository.save(user);
        return token;
    }

    @Transactional
    public void resetPasswordWithToken(String token, String newPassword, String confirmPassword) {
        if (token == null || token.isBlank()) {
            throw new BusinessException("INVALID_TOKEN", "Mã khôi phục không hợp lệ.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("WEAK_PASSWORD", "Mật khẩu mới phải từ 6 ký tự trở lên.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessException("PASSWORD_MISMATCH", "Xác nhận mật khẩu không khớp.");
        }
        User user = userRepository.findByResetPasswordToken(token.trim().toUpperCase())
                .orElseThrow(() -> new BusinessException("INVALID_TOKEN", "Mã khôi phục không tồn tại hoặc đã sử dụng."));
        if (user.getResetTokenExpiry() == null || LocalDateTime.now().isAfter(user.getResetTokenExpiry())) {
            throw new BusinessException("EXPIRED_TOKEN", "Mã khôi phục đã hết hạn (quá 30 phút).");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }
}
