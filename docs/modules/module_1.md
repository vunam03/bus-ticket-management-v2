# Tài Liệu Module 1 – Quản Lý Chuyến Xe & Nhà Xe

## 1. Phạm vi & Trách nhiệm
Module 1 (`com.busbooking.module_1`) phụ trách toàn bộ nghiệp vụ quản trị danh mục **Nhà xe (`BusCompany`)** và **Chuyến xe (`BusTrip`)** dành cho Admin, bao gồm bộ kiểm tra dữ liệu đầu vào (`TripValidator`) và trình tự động sinh sơ đồ ghế (`36`, `40`, `45` chỗ) khi tạo chuyến mới.

## 2. Danh sách File trong Module 1
- **Java (`src/main/java/com/busbooking/module_1/`)**:
  - `controller/AdminTripController.java` (`/admin/trips`, `/admin/trips/new`, `/admin/trips/{id}/edit`, `/admin/trips/{id}/cancel`, `/admin/trips/{id}/delete`)
  - `controller/AdminCompanyController.java` (`/admin/companies`, `/admin/companies/{id}/edit`, `/admin/companies/{id}/toggle`)
  - `service/BusTripService.java`
  - `service/BusCompanyService.java`
  - `validator/TripValidator.java`
  - `repository/BusTripRepository.java`, `BusCompanyRepository.java`, `TripSeatGeneratorRepository.java`
  - `dto/BusTripFormDto.java`, `BusCompanyFormDto.java`
- **Giao diện & Tĩnh**:
  - `templates/module_1/trip-list.html`, `trip-form.html`, `company-list.html`
  - `static/css/module_1/trips.css`
  - `static/js/module_1/trip-form.js`
- **Kiểm thử**:
  - `test/java/com/busbooking/module_1/Module1TripCompanyTest.java`

## 3. Quy tắc Nghiệp vụ Nổi bật
- Khi tạo chuyến xe mới, hệ thống tự động tạo đủ số lượng ghế theo `BusType`:
  - `LIMOUSINE_36`: 36 phòng đôi VIP chia 2 tầng (`A01..A18`, `B01..B18`).
  - `SLEEPER_40`: 40 giường nằm tiêu chuẩn chia 2 tầng (`A01..A20`, `B01..B20`).
  - `SEATER_45`: 45 ghế ngồi tiêu chuẩn 1 tầng (`S01..S45`).
- Khi hủy chuyến hoặc xóa chuyến đã có vé khách đặt: Hệ thống **không xóa dữ liệu vé lịch sử của khách**, mà chuyển trạng thái chuyến sang `CANCELLED`.
