package com.busbooking.module_2;

import com.busbooking.module_2.dto.ChangePasswordDto;
import com.busbooking.module_2.dto.RegisterRequestDto;
import com.busbooking.module_2.service.AuthAccountService;
import com.busbooking.module_2.service.CustomUserDetailsService;
import com.busbooking.common.enums.RoleName;
import com.busbooking.module_2.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class Module2AuthSecurityTest {

    @Autowired
    private AuthAccountService authAccountService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Module 2: Đăng ký tài khoản khách hàng mới mã hóa BCrypt và gán quyền ROLE_CUSTOMER")
    void shouldRegisterCustomerWithBcryptAndRole() {
        RegisterRequestDto req = new RegisterRequestDto();
        req.setUsername("newcustomer99");
        req.setFullName("Trần Bảo Ngọc");
        req.setEmail("baongoc99@busbooking.vn");
        req.setPhone("0988776655");
        req.setPassword("Customer@123");
        req.setConfirmPassword("Customer@123");

        User saved = authAccountService.registerCustomer(req);
        assertNotNull(saved.getId());
        assertEquals(RoleName.ROLE_CUSTOMER, saved.getRole().getName());
        assertTrue(passwordEncoder.matches("Customer@123", saved.getPasswordHash()));

        UserDetails byUsername = userDetailsService.loadUserByUsername("newcustomer99");
        UserDetails byEmail = userDetailsService.loadUserByUsername("baongoc99@busbooking.vn");
        assertEquals(byUsername.getUsername(), byEmail.getUsername());
    }

    @Test
    @DisplayName("Module 2: Đổi mật khẩu kiểm tra mật khẩu hiện tại và cập nhật BCrypt")
    void shouldChangePasswordSuccessfully() {
        RegisterRequestDto req = new RegisterRequestDto();
        req.setUsername("userpasschange");
        req.setFullName("Lê Văn Đổi Mật Khẩu");
        req.setEmail("passchange@busbooking.vn");
        req.setPhone("0911223344");
        req.setPassword("OldPass@123");
        req.setConfirmPassword("OldPass@123");
        authAccountService.registerCustomer(req);

        ChangePasswordDto changeDto = new ChangePasswordDto();
        changeDto.setCurrentPassword("OldPass@123");
        changeDto.setNewPassword("NewPass@456");
        changeDto.setConfirmPassword("NewPass@456");

        authAccountService.changePassword("userpasschange", changeDto);
        User updated = authAccountService.getByUsername("userpasschange");
        assertTrue(passwordEncoder.matches("NewPass@456", updated.getPasswordHash()));
    }
}
