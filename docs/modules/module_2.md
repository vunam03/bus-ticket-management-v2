# Tài Liệu Module 2 – Đăng Nhập, Phân Quyền & Tài Khoản

## 1. Phạm vi & Trách nhiệm
Module 2 (`com.busbooking.module_2`) quản lý xác thực người dùng, đăng ký tài khoản khách hàng, đăng nhập bằng tên đăng nhập hoặc email, quên mật khẩu, đổi mật khẩu, cập nhật hồ sơ cá nhân và bảo vệ phân quyền `ROLE_ADMIN` / `ROLE_CUSTOMER` thông qua Spring Security.

## 2. Danh sách File trong Module 2
- **Java (`src/main/java/com/busbooking/module_2/`)**:
  - `controller/AuthController.java` (`/auth/login`, `/auth/register`, `/auth/forgot-password`)
  - `controller/UserProfileController.java` (`/account/profile`, `/account/change-password`)
  - `service/CustomUserDetailsService.java`, `AuthAccountService.java`
  - `repository/UserAccountRepository.java`, `RoleRepository.java`
  - `dto/RegisterRequestDto.java`, `ProfileUpdateDto.java`, `ChangePasswordDto.java`, `ForgotPasswordDto.java`
- **Giao diện & Tĩnh**:
  - `templates/module_2/login.html`, `register.html`, `forgot-password.html`, `profile.html`
  - `static/css/module_2/auth.css`
  - `static/js/module_2/auth.js`
- **Kiểm thử**:
  - `test/java/com/busbooking/module_2/Module2AuthSecurityTest.java`

## 3. Quy tắc Bảo mật & Phân quyền
- Mật khẩu luôn được băm bằng `BCryptPasswordEncoder`.
- Mọi đường dẫn `/admin/**` yêu cầu quyền `ROLE_ADMIN`. Khách hàng truy cập sẽ được chuyển hướng tới trang thông báo từ chối truy cập `/auth/access-denied`.
- Sau khi `ADMIN` đăng nhập thành công:
  1. Tự động điều hướng tới `/admin/dashboard`.
  2. Trên thanh `Navbar` dùng chung xuất hiện nổi bật nút **"Quản trị Admin"**.
