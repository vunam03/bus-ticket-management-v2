# Tài Liệu Module 6 – Dashboard Admin, Thống Kê & Xuất Báo Cáo Excel

## 1. Phạm vi & Trách nhiệm (Module Phụ Trách Chính)
Module 6 (`com.busbooking.module_6`) cung cấp trung tâm điều hành và phân tích dữ liệu thời gian thực cho Admin:
- Tổng hợp 4 KPI cốt lõi: Tổng doanh thu thực thu, Tổng chuyến xe, Tổng vé bán, Tổng khách hàng.
- Hệ thống biểu đồ trực quan bằng **Chart.js**: Biểu đồ cột doanh thu 12 tháng (hỗ trợ đổi năm qua AJAX API), Biểu đồ đường xu hướng 14 ngày, Biểu đồ Donut cơ cấu trạng thái vé.
- Thống kê doanh thu và tỷ trọng vé theo **Nhà xe** và theo **Tuyến đường**.
- Thống kê **Tỷ lệ lấp đầy ghế (Seat Occupancy Rate)** của từng chuyến xe.
- Bảng giao dịch gần nhất & Trang phân tích chi tiêu khách hàng có tìm kiếm, phân trang và xếp hạng thành viên.
- Báo cáo doanh thu lọc theo khoảng ngày (`startDate`, `endDate`), nhà xe, tuyến đường và **Xuất file Excel (`.xlsx`) 3 Sheet** bằng Apache POI.

## 2. Danh sách Class & File trong Module 6 (> 3.350 dòng code thực chất)
- **Controllers**:
  - `AdminDashboardController.java` (`/admin/dashboard`, `/admin/dashboard/customers`, `/admin/dashboard/api/revenue-chart`)
  - `ReportController.java` (`/admin/reports`, `/admin/reports/revenue`, `/admin/reports/export`)
- **Services & Exporter**:
  - `StatisticService.java`
  - `ReportService.java`
  - `ExportService.java`
  - `ExcelExporter.java`
- **DTOs**:
  - `DashboardSummaryDto.java`, `RevenueStatisticDto.java`, `TicketSalesStatDto.java`, `MonthlyReportDto.java`, `CustomerAnalyticsDto.java`, `SeatOccupancyDto.java`, `RecentTransactionDto.java`, `ReportFilterDto.java`
- **Repositories**:
  - `DashboardTicketRepository.java`, `DashboardTripRepository.java`, `DashboardCustomerRepository.java`
- **Giao diện, CSS, JS & Kiểm thử**:
  - `templates/module_6/dashboard.html`, `revenue-report.html`, `customer-list.html`
  - `static/css/module_6/dashboard.css`, `reports.css`
  - `static/js/module_6/revenue-chart.js`, `dashboard.js`, `report-filter.js`
  - `test/java/com/busbooking/module_6/Module6DashboardAnalyticsTest.java`
