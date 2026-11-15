# Tài Liệu Module 4 – Chọn Ghế & Giữ Ghế Tạm Thời

## 1. Phạm vi & Trách nhiệm
Module 4 (`com.busbooking.module_4`) hiển thị sơ đồ ghế trực quan theo cấu hình chuyến xe (36/40/45 ghế, 1 hoặc 2 tầng), tính tổng tiền dự kiến theo thời gian thực khi chọn/bỏ chọn ghế, giữ ghế tạm thời trong 10 phút và tự động giải phóng ghế khi hết hạn giữ.

## 2. Danh sách File trong Module 4
- **Java (`src/main/java/com/busbooking/module_4/`)**:
  - `controller/SeatSelectionController.java` (`/booking/trip/{tripId}/seats`, `/booking/trip/{tripId}/hold`, `/booking/hold/release/{holdCode}`)
  - `service/SeatSelectionService.java`
  - `repository/SeatSelectionRepository.java`, `SeatHoldRepository.java`
  - `dto/SeatHoldResultDto.java`
- **Giao diện & Tĩnh**:
  - `templates/module_4/seat-selection.html`
  - `static/css/module_4/seat-map.css`
  - `static/js/module_4/seat-selection.js`
- **Kiểm thử**:
  - `test/java/com/busbooking/module_4/Module4SeatHoldTest.java`

## 3. Cơ chế Chống Đặt Trùng & Tự Giải Phóng Ghế
- Sử dụng `@Version` (Optimistic Locking) trên thực thể `Seat` kết hợp kiểm tra trạng thái `AVAILABLE` trong giao dịch `@Transactional`.
- Tiến trình nền `@Scheduled(fixedDelay = 30000)` tự động quét các bản ghi `SeatHold` hết hạn 10 phút để trả ghế về `AVAILABLE` và cập nhật lại số ghế trống của chuyến xe.
