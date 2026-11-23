# Hệ thống Quản lý Đặt vé Xe khách Trực tuyến (BusBooking VN)

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen?style=flat-square&logo=springboot)
![Database](https://img.shields.io/badge/Database-H2%20%7C%20MySQL-blue?style=flat-square&logo=mysql)
![Frontend](https://img.shields.io/badge/Frontend-Thymeleaf%20%7C%20Bootstrap%205%20%7C%20Chart.js-7952B3?style=flat-square)

Ứng dụng web đặt vé xe khách trực tuyến được xây dựng theo kiến trúc **Modular Monolith 6 Module độc lập** trên nền tảng **Spring Boot 4.1.1 (Java 17)**, hỗ trợ đầy đủ quy trình từ quản trị chuyến xe, tìm kiếm & lọc tuyến, giữ chỗ chống đặt trùng đồng thời, thanh toán mô phỏng, xuất vé PDF/QR SVG cho đến thống kê Dashboard & xuất báo cáo Excel.

---

## Mục lục

1. [Công nghệ sử dụng](#-công-nghệ-sử-dụng)
2. [Kiến trúc & Cấu trúc thư mục](#-kiến-trúc--cấu-trúc-thư-mục)
3. [Tính năng chính theo 6 Module](#-tính-năng-chính-theo-6-module)
4. [Cơ chế nghiệp vụ nổi bật](#-cơ-chế-nghiệp-vụ-nổi-bật)
5. [Hướng dẫn cài đặt & Khởi chạy](#-hướng-dẫn-cài-đặt--khởi-chạy)
6. [Địa chỉ truy cập & Tài khoản mẫu](#-địa-chỉ-truy-cập--tài-khoản-mẫu)
7. [Kiểm thử tự động](#-kiểm-thử-tự-động)
8. [Phân công thành viên (Nhóm 03)](#-phân-công-thành-viên-nhóm-03)

---

## Công nghệ sử dụng

| Thành phần | Công nghệ / Phiên bản |
| :--- | :--- |
| **Ngôn ngữ & Runtime** | Java 17 |
| **Backend Framework** | Spring Boot 4.1.1 (Spring Web MVC, Spring Data JPA, Spring Security, Validation) |
| **Template Engine & UI** | Thymeleaf, Bootstrap 5, Chart.js |
| **Cơ sở dữ liệu** | H2 Database (mặc định qua profile `local`), MySQL (`mysql-connector-j`) |
| **Xuất báo cáo & Tiện ích** | Apache POI `5.4.1` (Excel `.xlsx`), sinh mã QR SVG, xuất vé PDF |
| **Build & Test** | Maven Wrapper (`mvnw` / `mvnw.cmd`), JUnit 5, Spring Test / Spring Security Test |

---

## Kiến trúc & Cấu trúc thư mục

Dự án tổ chức mã nguồn theo mô hình **6 Module độc lập**, dùng chung các tiện ích nền tảng trong `common/` và cấu hình toàn cục trong `config/`:

```text
bus-ticket-management-v2/
├── docs/                                # Tài liệu kiến trúc, hướng dẫn test & đặc tả 6 module
├── src/
│   ├── main/
│   │   ├── java/com/busbooking/
│   │   │   ├── BusBookingApplication.java
│   │   │   ├── common/                  # Enums, Exceptions (GlobalExceptionHandler), Utils (QR, PDF, CodeGen)
│   │   │   ├── config/                  # SecurityConfig, H2ConsoleConfig, GlobalModelAdvice, DatabaseSeederConfig
│   │   │   ├── module_1/                # Quản lý Nhà xe & Chuyến xe (Admin) + tự động sinh sơ đồ ghế
│   │   │   ├── module_2/                # Đăng ký, Đăng nhập, Phân quyền & Hồ sơ cá nhân
│   │   │   ├── module_3/                # Trang chủ, Tìm kiếm & Bộ lọc chuyến xe
│   │   │   ├── module_4/                # Sơ đồ ghế 36/40/45 chỗ & Giữ ghế tạm thời 10 phút
│   │   │   ├── module_5/                # Đặt vé, Thanh toán, Mã QR SVG, Vé PDF & Hoàn hủy vé
│   │   │   └── module_6/                # Dashboard Admin, Biểu đồ Chart.js, Thống kê & Xuất Excel
│   │   └── resources/
│   │       ├── application.properties       # Cấu hình chung & MySQL (qua biến môi trường)
│   │       ├── application-local.properties # Cấu hình mặc định dùng H2 file DB
│   │       ├── static/                      # CSS & JS tách riêng theo từng module_X
│   │       └── templates/                   # Giao diện Thymeleaf theo từng module_X
│   └── test/java/com/busbooking/
│       ├── module_1/Module1TripCompanyTest.java
│       ├── module_2/Module2AuthSecurityTest.java
│       ├── module_3/Module3SearchFilterTest.java
│       ├── module_4/Module4SeatHoldTest.java
│       ├── module_5/Module5BookingPaymentTest.java
│       └── module_6/
│           ├── Module6DashboardAnalyticsTest.java
│           └── ModuleViewRenderTest.java
└── pom.xml
```

---

## Tính năng chính theo 6 Module

- **Module 1 – Quản lý Nhà xe & Chuyến xe (`module_1`)**
    - Quản lý danh mục nhà xe (`BusCompany`) và chuyến xe (`BusTrip`).
    - Kiểm tra tính hợp lệ dữ liệu (`BusTripValidator`).
    - Tự động sinh sơ đồ ghế (`36`, `40`, `45` chỗ) tương ứng với loại xe (`LIMOUSINE_36`, `SLEEPER_36`, `SLEEPER_40`, `SEATER_45`) khi khởi tạo chuyến mới.
- **Module 2 – Xác thực, Phân quyền & Tài khoản (`module_2`)**
    - Đăng ký, đăng nhập (bằng username hoặc email), đổi mật khẩu, quên/đặt lại mật khẩu, cập nhật hồ sơ cá nhân.
    - Phân quyền chặt chẽ `ROLE_ADMIN` và `ROLE_CUSTOMER` với Spring Security và mã hóa mật khẩu `BCryptPasswordEncoder`.
- **Module 3 – Tìm kiếm & Khám phá Chuyến xe (`module_3`)**
    - Trang chủ và cổng khám phá tuyến xe liên tỉnh.
    - Tìm kiếm theo tỉnh/thành đi – đến, ngày khởi hành; lọc nâng cao theo khoảng giá, loại xe, khung giờ xuất bến, nhà xe và sắp xếp kết quả.
- **Module 4 – Sơ đồ ghế & Giữ ghế tạm thời (`module_4`)**
    - Hiển thị sơ đồ ghế trực quan 1 tầng / 2 tầng, cập nhật trạng thái ghế theo thời gian thực qua AJAX polling.
    - Chọn tối đa 6 ghế/lần đặt, tính tổng tiền tức thì và giữ ghế tạm thời 10 phút kèm đồng hồ đếm ngược.
- **Module 5 – Đặt vé, Thanh toán, QR & PDF (`module_5`)**
    - Xác nhận đặt vé từ phiên giữ ghế (`holdCode`), hỗ trợ 3 phương thức thanh toán mô phỏng: Chuyển khoản (`BANK_TRANSFER`), Ví điện tử (`E_WALLET`), Tiền mặt (`CASH`).
    - Sinh thẻ lên xe điện tử kèm **mã QR SVG**, hỗ trợ in vé và tải file **vé PDF**.
    - Quản lý lịch sử **Vé của tôi** và xử lý hủy vé – hoàn tiền tự động.
- **Module 6 – Dashboard Quản trị & Báo cáo (`module_6`)**
    - Tổng hợp 4 KPI cốt lõi (Tổng doanh thu thực thu, Tổng chuyến xe, Tổng vé bán, Tổng khách hàng).
    - Biểu đồ tương tác **Chart.js** (doanh thu 12 tháng theo năm, xu hướng 14 ngày gần nhất, cơ cấu trạng thái vé).
    - Xuất báo cáo doanh thu ra file **Excel (`.xlsx`) 3 Sheet** bằng Apache POI và hỗ trợ duyệt thanh toán tiền mặt / hủy vé phía Admin.

---

## Cơ chế nghiệp vụ nổi bật

- **Chống đặt trùng ghế đồng thời (4 lớp bảo vệ):**
    1. Sắp xếp ID ghế tăng dần (**Lock Ordering**) trước khi khóa để chống Deadlock.
    2. Khóa bi quan ở tầng CSDL (`PESSIMISTIC_WRITE`).
    3. Khóa lạc quan (`@Version`) trên Entity `Seat`.
    4. Ràng buộc duy nhất (`UNIQUE`) trên bảng giữ ghế `seat_holds`.
- **Giải phóng ghế tự động:** Tác vụ nền `@Scheduled` chạy định kỳ mỗi **30 giây** để thu hồi các phiên giữ ghế hết hạn (`10 phút`, gia hạn tối đa 1 lần lên `15 phút`).
- **Chính sách hủy vé & hoàn tiền:**
    - Hủy trước giờ khởi hành **≥ 24 giờ**: hoàn **90%** giá vé.
    - Hủy trước giờ khởi hành từ **4 giờ đến < 24 giờ**: hoàn **70%** giá vé.
    - Dưới **4 giờ** trước giờ khởi hành: không hỗ trợ hủy vé trực tuyến.

---

## Hướng dẫn cài đặt & Khởi chạy

### Yêu cầu môi trường
- **JDK 17** trở lên
- **MySQL 8.0+** *(tùy chọn — mặc định ứng dụng chạy ngay bằng H2 File Database mà không cần cài MySQL)*

### Cách 1: Chạy nhanh với H2 Database (Mặc định - Profile `local`)

Ứng dụng mặc định kích hoạt `spring.profiles.default=local`, lưu dữ liệu tại `./work/local-db/bus-ticket-v2` và tự động nạp sẵn dữ liệu mẫu (nhà xe, chuyến xe, tài khoản, vé mẫu) khi chạy lần đầu.

**Trên Windows (PowerShell / CMD):**
```powershell
git clone <url-repository>
cd bus-ticket-management-v2
.\mvnw.cmd spring-boot:run
```

**Trên macOS / Linux / Git Bash:**
```bash
git clone <url-repository>
cd bus-ticket-management-v2
./mvnw spring-boot:run
```

### Cách 2: Chạy với MySQL

1. Đảm bảo dịch vụ MySQL đang chạy tại `localhost:3306` (ứng dụng tự tạo schema `bus_ticket_v2` nếu chưa có).
2. Thiết lập biến môi trường (nếu username/password khác mặc định `root` / `1234`) và tắt profile `local` bằng cách truyền profile `mysql`:

```powershell
# PowerShell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=mysql"
```

---

## Địa chỉ truy cập & Tài khoản mẫu

Sau khi ứng dụng khởi động thành công tại cổng `8080`:

| Trang / Chức năng | Đường dẫn URL |
| :--- | :--- |
| **Trang chủ khách hàng** | [http://localhost:8080/](http://localhost:8080/) |
| **Cổng khám phá tuyến xe** | [http://localhost:8080/index](http://localhost:8080/index) |
| **Đăng nhập / Đăng ký** | [http://localhost:8080/auth/login](http://localhost:8080/auth/login) · [http://localhost:8080/auth/register](http://localhost:8080/auth/register) |
| **Vé của tôi** | [http://localhost:8080/tickets/my-tickets](http://localhost:8080/tickets/my-tickets) |
| **Dashboard Quản trị (Admin)** | [http://localhost:8080/admin/dashboard](http://localhost:8080/admin/dashboard) |
| **Quản lý Chuyến xe / Nhà xe / Vé** | `/admin/trips` · `/admin/companies` · `/admin/tickets` |
| **Báo cáo Doanh thu & Xuất Excel** | [http://localhost:8080/admin/reports/revenue](http://localhost:8080/admin/reports/revenue) |
| **H2 Database Console** *(profile `local`)* | [http://localhost:8080/h2-console](http://localhost:8080/h2-console) |

> **Thông tin kết nối H2 Console:** JDBC URL `jdbc:h2:file:./work/local-db/bus-ticket-v2` · User Name: `sa` · Password: *(để trống)*

### Tài khoản thử nghiệm có sẵn

| Vai trò | Tên đăng nhập | Mật khẩu | Ghi chú |
| :--- | :--- | :--- | :--- |
| **Quản trị viên (`ROLE_ADMIN`)** | `admin` | `Admin@123456` | Toàn quyền `/admin/**`, duyệt tiền mặt, xuất Excel |
| **Khách hàng 1 (`ROLE_CUSTOMER`)** | `nguyenvanan` | `123456` | Có sẵn vé mẫu để xem QR, tải PDF, thử hủy vé |
| **Khách hàng 2 (`ROLE_CUSTOMER`)** | `tranthibich` | `123456` | Kiểm thử đặt vé & chống chọn trùng ghế |
| **Khách hàng 3 (`ROLE_CUSTOMER`)** | `lehoangnam` | `123456` | Kiểm thử hồ sơ, đổi mật khẩu, lịch sử đặt vé |

---

## Kiểm thử tự động

Chạy toàn bộ bộ kiểm thử JUnit 5 trên cả 6 module:

```powershell
# Windows
.\mvnw.cmd test

# macOS / Linux / Git Bash
./mvnw test
```

*(Chi tiết thêm về kịch bản kiểm thử thủ công có thể xem tại `docs/HUONG_DAN_TEST.md`.)*

---

## Phân công thành viên (Nhóm 03)

| STT | Thành viên | Module phụ trách | Nội dung chính |
| :---: | :--- | :--- | :--- |
| 1 | **Trịnh Vũ Nam** | Module 1 | Quản lý chuyến xe, nhà xe & sinh sơ đồ ghế |
| 2 | **Bùi Lê Minh** | Module 2 | Đăng ký, đăng nhập, phân quyền & hồ sơ |
| 3 | **Đỗ Hữu Vũ** | Module 3 | Trang chủ, tìm kiếm & lọc chuyến xe |
| 4 | **Phạm Văn Linh** | Module 4 | Sơ đồ ghế & giữ ghế tạm thời 10 phút |
| 5 | **Tôn Gia Diễn** | Module 5 | Đặt vé, thanh toán, QR SVG, PDF & hoàn hủy |
| 6 | **Nguyễn Thành Trung** | Module 6 | Dashboard Admin, biểu đồ Chart.js & xuất Excel |