# Tài Liệu Module 5 – Đặt Vé, Thanh Toán, QR & PDF

## 1. Phạm vi & Trách nhiệm
Module 5 (`com.busbooking.module_5`) thực hiện bước xác nhận đặt vé từ phiên giữ ghế, xử lý thanh toán mô phỏng (`CASH`, `BANK_TRANSFER`, `E_WALLET`), lưu giao dịch (`PaymentTransaction`), quản lý danh sách "Vé của tôi", sinh mã QR SVG, xuất vé PDF tải về, in vé và xử lý hủy vé hoàn tiền theo chính sách.

## 2. Danh sách File trong Module 5
- **Java (`src/main/java/com/busbooking/module_5/`)**:
  - `controller/TicketBookingController.java` (`/booking/checkout`, `/booking/confirm`, `/tickets/my-tickets`, `/tickets/{id}`, `/tickets/{id}/print`, `/tickets/{id}/pdf`, `/tickets/{id}/cancel`)
  - `controller/AdminTicketPaymentController.java` (`/admin/tickets`, `/admin/payments`, `/admin/tickets/{id}/confirm-payment`, `/admin/tickets/{id}/cancel`)
  - `service/TicketBookingService.java`, `QrCodeAndPdfService.java`
  - `repository/BookingTicketRepository.java`, `PaymentTransactionRepository.java`
  - `dto/BookingCheckoutDto.java`
- **Giao diện & Tĩnh**:
  - `templates/module_5/checkout.html`, `my-tickets.html`, `ticket-detail.html`, `ticket-print.html`, `admin-tickets.html`, `admin-payments.html`
  - `static/css/module_5/booking.css`
  - `static/js/module_5/checkout.js`
- **Kiểm thử**:
  - `test/java/com/busbooking/module_5/Module5BookingPaymentTest.java`

## 3. Chính sách Hoàn tiền khi Hủy vé
- Hủy vé trước giờ khởi hành **từ 24 tiếng trở lên**: Hoàn **90%** giá trị vé đã thanh toán.
- Hủy vé trước giờ khởi hành **dưới 24 tiếng**: Hoàn **70%** giá trị vé đã thanh toán.
- Giải phóng ngay ghế về trạng thái `AVAILABLE` và tạo giao dịch hoàn tiền `REFUNDED`.
