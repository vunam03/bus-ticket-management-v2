package com.busbooking.module_2.service;

import com.busbooking.common.enums.RoleName;
import com.busbooking.common.exception.ResourceNotFoundException;
import com.busbooking.module_2.dto.ChangePasswordRequest;
import com.busbooking.module_2.dto.ProfileUpdateDto;
import com.busbooking.module_2.dto.UserProfileResponse;
import com.busbooking.module_2.exception.AuthAccountException;
import com.busbooking.module_2.model.User;
import com.busbooking.module_2.repository.UserRepository;
import com.busbooking.module_2.security.SecurityPasswordPolicyValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service quản lý thông tin người dùng, hồ sơ cá nhân và bảo mật mật khẩu (Module 2).
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityPasswordPolicyValidator policyValidator;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       SecurityPasswordPolicyValidator policyValidator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.policyValidator = policyValidator;
    }

    @Transactional(readOnly = true)
    public User getUserByUsernameOrEmail(String identifier) {
        String cleanId = identifier == null ? "" : identifier.trim();
        return userRepository.findByUsernameOrEmail(cleanId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản: " + cleanId));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfileSummary(String username) {
        return UserProfileResponse.fromEntity(getUserByUsernameOrEmail(username));
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> getCustomerProfiles() {
        return userRepository.findByRoleNameOrderByCreatedAtDesc(RoleName.ROLE_CUSTOMER)
                .stream()
                .map(UserProfileResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countCustomers() {
        return userRepository.countByRoleName(RoleName.ROLE_CUSTOMER);
    }

    @Transactional
    public UserProfileResponse updateProfile(String username, ProfileUpdateDto dto) {
        User user = getUserByUsernameOrEmail(username);
        String newEmail = policyValidator.normalizeEmail(dto.getEmail());
        String newPhone = policyValidator.normalizePhone(dto.getPhone());

        userRepository.findByEmailIgnoreCase(newEmail).ifPresent(existing -> {
            if (!existing.getId().equals(user.getId())) {
                throw AuthAccountException.emailTaken(newEmail);
            }
        });

        user.setFullName(dto.getFullName().trim());
        user.setEmail(newEmail);
        user.setPhone(newPhone);
        user.setAddress(dto.getAddress() != null ? dto.getAddress().trim() : null);
        return UserProfileResponse.fromEntity(userRepository.save(user));
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = getUserByUsernameOrEmail(username);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw AuthAccountException.invalidCurrentPassword();
        }
        policyValidator.validatePasswordPair(request.getNewPassword(), request.getConfirmPassword());
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw AuthAccountException.weakPassword("Mật khẩu mới phải khác với mật khẩu hiện tại.");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public boolean toggleAccountEnabled(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng ID: " + userId));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        return user.isEnabled();
    }
}
