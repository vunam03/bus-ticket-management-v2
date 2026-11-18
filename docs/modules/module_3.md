# Tài Liệu Module 3 – Tìm Kiếm & Lọc Chuyến Xe

## 1. Phạm vi & Trách nhiệm
Module 3 (`com.busbooking.module_3`) cung cấp giao diện Trang chủ, công cụ tìm kiếm chuyến xe theo lộ trình/ngày khởi hành, bộ lọc nâng cao (khoảng giá, loại xe, khung giờ xuất bến, nhà xe), sắp xếp kết quả và xem chi tiết từng chuyến xe.

## 2. Danh sách File trong Module 3
- **Java (`src/main/java/com/busbooking/module_3/`)**:
    - `controller/HomeSearchController.java` (`/`, `/trips/search`, `/trips/{id}`)
    - `service/TripSearchService.java`
    - `repository/TripSearchRepository.java`
    - `dto/TripSearchFilterDto.java`
- **Giao diện & Tĩnh**:
    - `templates/module_3/home.html`, `search-results.html`, `trip-detail.html`
    - `static/css/module_3/search.css`
    - `static/js/module_3/search-filter.js`
- **Kiểm thử**:
    - `test/java/com/busbooking/module_3/Module3SearchFilterTest.java`

## 3. Tính năng Lọc & Sắp xếp
- **4 khung giờ xuất bến**: Buổi sáng (`05:00-11:59`), Buổi chiều (`12:00-17:59`), Buổi tối (`18:00-21:59`), Ban đêm (`22:00-04:59`).
- **5 chế độ sắp xếp**: Giờ chạy sớm nhất, Giờ chạy muộn nhất, Giá thấp đến cao, Giá cao xuống thấp, Nhiều ghế trống nhất.
