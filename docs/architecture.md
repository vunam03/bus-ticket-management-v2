# Kiến Trúc Tổng Thể Hệ Thống Đặt Vé Xe Khách Trực Tuyến (v2.0)

## 1. Tổng quan Kiến trúc 6 Module Độc lập

Dự án `bus-ticket-management-v2` được thiết kế theo mô hình **Modular Monolith 6 Module** trên một ứng dụng **Spring Boot 4.1.1 (Java 17)** duy nhất. Mỗi thành viên trong nhóm phụ trách một thư mục `module_X` độc lập từ tầng Backend (`controller`, `service`, `repository`, `dto`) cho tới tầng Giao diện (`templates/module_X`, `static/css/module_X`, `static/js/module_X`) và Kiểm thử (`test/java/com/busbooking/module_X`), đồng thời liên kết chặt chẽ qua các Entity và tiện ích dùng chung trong `shared/`.

```text
com.busbooking
├── BusBookingApplication.java
├── config/          (SecurityConfig, GlobalModelAdvice, DatabaseSeederConfig)
├── shared/          (8 Entities, 8 Enums, GlobalExceptionHandler, Utils)
├── module_1/        (Quản lý Chuyến xe & Nhà xe - Admin)
├── module_2/        (Đăng ký, Đăng nhập, Phân quyền & Hồ sơ cá nhân)
├── module_3/        (Trang chủ, Tìm kiếm & Bộ lọc Chuyến xe)
├── module_4/        (Sơ đồ ghế 36/40/45 chỗ & Giữ ghế tạm thời 10 phút)
├── module_5/        (Đặt vé, Thanh toán, Sinh QR SVG, Xuất PDF & Hoàn hủy vé)
└── module_6/        (Dashboard Admin, Biểu đồ Chart.js, Thống kê & Xuất Excel)
```

---

## 2. Sơ đồ Quan hệ Thực thể Dùng chung (`shared/model`)

Hệ thống sử dụng **8 Entity chính duy nhất**, không tạo trùng lặp bảng hay Entity giữa các module:

1. **`Role` (`roles`)**: Lưu vai trò `ROLE_ADMIN` và `ROLE_CUSTOMER`.
2. **`User` (`users`)**: Tài khoản người dùng, liên kết `ManyToOne` tới `Role`. Mật khẩu mã hóa bằng `BCryptPasswordEncoder`.
3. **`BusCompany` (`bus_companies`)**: Thông tin nhà xe (Mã nhà xe, tên, hotline, địa chỉ, trạng thái hoạt động).
4. **`BusTrip` (`bus_trips`)**: Chuyến xe khách, liên kết `ManyToOne` tới `BusCompany`, lưu điểm đi/đến, bến đi/đến, giờ khởi hành/đến, giá vé, loại xe (`LIMOUSINE_36`, `SLEEPER_40`, `SEATER_45`), tổng ghế và ghế trống.
5. **`Seat` (`seats`)**: Ghế ngồi/giường nằm của từng chuyến xe, liên kết `ManyToOne` tới `BusTrip`, có `@Version` (Optimistic Locking) chống hai người đặt trùng ghế cùng lúc.
6. **`SeatHold` (`seat_holds`)**: Phiên giữ ghế tạm thời 10 phút (`holdCode`, `expiresAt`, `released`), liên kết tới `Seat` và `User`.
7. **`Ticket` (`tickets`)**: Vé xe điện tử (`ticketCode`, thông tin hành khách, giá vé, trạng thái vé, trạng thái thanh toán, mã QR, thông tin hủy & hoàn tiền).
8. **`PaymentTransaction` (`payment_transactions`)**: Lịch sử giao dịch thanh toán và hoàn tiền (`CASH`, `BANK_TRANSFER`, `E_WALLET`).

---

## 3. Luồng Nghiệp vụ Liên kết Giữa 6 Module

1. **Module 1** tạo `BusCompany` và `BusTrip` -> tự động khởi tạo 36/40/45 bản ghi `Seat` tương ứng.
2. **Module 2** xác thực người dùng (`ROLE_ADMIN` / `ROLE_CUSTOMER`), hiển thị nút **Quản trị Admin** trên Navbar khi đăng nhập quyền Admin.
3. **Module 3** truy vấn các chuyến xe `OPEN`, lọc theo tỉnh đi/đến, ngày đi, khoảng giá, khung giờ, loại xe, nhà xe.
4. **Module 4** hiển thị sơ đồ ghế trực quan 1 hoặc 2 tầng, khóa ghế tạm thời 10 phút (`SeatHold`) và tự động giải phóng ghế quá hạn mỗi 30 giây (`@Scheduled`).
5. **Module 5** tiếp nhận `holdCode` từ Module 4, xử lý thanh toán, phát hành `Ticket` + `PaymentTransaction`, tạo mã QR SVG và vé PDF tải về, hoặc xử lý hoàn tiền 90%/70% khi hủy vé.
6. **Module 6** tổng hợp toàn bộ dữ liệu thực tế từ `Ticket`, `BusTrip`, `Seat`, `User`, `BusCompany` để hiển thị KPI Dashboard, vẽ biểu đồ `Chart.js` và xuất sổ tính Excel `.xlsx` 3 Sheet.
