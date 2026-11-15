package com.busbooking.module_1.exception;

import com.busbooking.common.exception.BusinessException;

/**
 * Ngoại lệ nghiệp vụ chuyên biệt cho Module 1 (Quản lý Nhà xe & Chuyến xe).
 */
public class TripManagementException extends BusinessException {

    public TripManagementException(String errorCode, String message) {
        super(errorCode, message);
    }

    public static TripManagementException invalidRoute(String departure, String arrival) {
        return new TripManagementException("INVALID_ROUTE",
                "Tỉnh/thành phố khởi hành (" + departure + ") và điểm đến (" + arrival + ") không được trùng nhau.");
    }

    public static TripManagementException invalidSchedule(String reason) {
        return new TripManagementException("INVALID_SCHEDULE", reason);
    }

    public static TripManagementException licensePlateConflict(String licensePlate) {
        return new TripManagementException("VEHICLE_SCHEDULE_CONFLICT",
                "Xe mang biển kiểm soát " + licensePlate + " đang có lịch chạy trùng khung giờ khác.");
    }

    public static TripManagementException companyInactive(String companyName) {
        return new TripManagementException("COMPANY_INACTIVE",
                "Nhà xe '" + companyName + "' hiện đang tạm ngưng hoạt động, không thể mở chuyến mới.");
    }

    public static TripManagementException lockedByActiveTickets(String tripCode) {
        return new TripManagementException("TRIP_LOCKED_BY_TICKETS",
                "Chuyến xe " + tripCode + " đã phát sinh vé hợp lệ của hành khách, không thể thay đổi loại sơ đồ ghế.");
    }
}
