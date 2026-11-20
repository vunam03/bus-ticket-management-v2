package com.busbooking.module_2.service;

import com.busbooking.module_2.repository.UserAccountRepository;
import com.busbooking.module_2.model.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userRepository;

    public CustomUserDetailsService(UserAccountRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        String cleanId = identifier == null ? "" : identifier.trim();
        User user = userRepository.findByUsernameOrEmail(cleanId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản với tên đăng nhập hoặc email: " + cleanId));

        String roleName = user.getRole().getName().name();
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                user.isEnabled(),
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority(roleName))
        );
    }
}
