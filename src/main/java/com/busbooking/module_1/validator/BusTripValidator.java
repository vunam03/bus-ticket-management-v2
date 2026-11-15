package com.busbooking.module_1.validator;

import com.busbooking.common.enums.TripStatus;
import com.busbooking.module_1.dto.BusTripFormDto;
import com.busbooking.module_1.exception.TripManagementException;
import com.busbooking.module_1.model.BusTrip;
import com.busbooking.module_1.repository.BusTripRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Bộ kiểm tra nghiệp vụ toàn diện cho biểu mẫu chuyến xe & quy tắc lịch trình (Module 1).
 */
@Component
public class BusTripValidator {

    private static final BigDecimal MIN_PRICE = new BigDecimal("10000");
    private static final BigDecimal MAX_PRICE = new BigDecimal("10000000");
    private static final long MIN_DURATION_MINUTES = 30L;
    private static final long MAX_DURATION_MINUTES = 72L * 60L;
    private static final Pattern LICENSE_PLATE_PATTERN = Pattern.compile("^[0-9]{2}[A-Z]{1,2}-?[0-9]{3,5}(\\.[0-9]{2})?$");

    private final BusTripRepository tripRepository;

    public BusTripValidator(BusTripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    public void validateTripForm(BusTripFormDto dto) {
        if (dto == null) {
            throw TripManagementException.invalidSchedule("Dữ liệu chuyến xe không được để trống.");
        }
        validateRoute(dto);
        validateSchedule(dto.getDepartureTime(), dto.getArrivalTime());
        validatePrice(dto.getTicketPrice());
        validateBusSpecification(dto);
    }

    public void validateRoute(BusTripFormDto dto) {
        String depProvince = safeTrim(dto.getDepartureProvince());
        String arrProvince = safeTrim(dto.getArrivalProvince());
        if (depProvince.isEmpty() || arrProvince.isEmpty()) {
            throw new TripManagementException("INVALID_PROVINCE", "Tỉnh/thành phố đi và đến không được để trống.");
        }
        if (depProvince.equalsIgnoreCase(arrProvince)) {
            throw TripManagementException.invalidRoute(depProvince, arrProvince);
        }

        String depStation = safeTrim(dto.getDepartureStation());
        String arrStation = safeTrim(dto.getArrivalStation());
        if (depStation.isEmpty() || arrStation.isEmpty()) {
            throw new TripManagementException("INVALID_STATION", "Điểm đón và điểm trả khách không được để trống.");
        }
    }

    public void validateSchedule(LocalDateTime departureTime, LocalDateTime arrivalTime) {
        if (departureTime == null || arrivalTime == null) {
            throw TripManagementException.invalidSchedule("Thời gian khởi hành và thời gian đến không được để trống.");
        }
        if (!arrivalTime.isAfter(departureTime)) {
            throw new TripManagementException("INVALID_TIME_RANGE", "Thời gian đến dự kiến phải sau thời gian khởi hành.");
        }
        long durationMinutes = Duration.between(departureTime, arrivalTime).toMinutes();
        if (durationMinutes < MIN_DURATION_MINUTES) {
            throw TripManagementException.invalidSchedule("Thời lượng hành trình phải ít nhất 30 phút.");
        }
        if (durationMinutes > MAX_DURATION_MINUTES) {
            throw TripManagementException.invalidSchedule("Thời lượng hành trình không được vượt quá 72 giờ.");
        }
    }

    public void validatePrice(BigDecimal ticketPrice) {
        if (ticketPrice == null || ticketPrice.compareTo(MIN_PRICE) < 0) {
            throw new TripManagementException("INVALID_PRICE", "Giá vé phải từ 10.000 VNĐ trở lên.");
        }
        if (ticketPrice.compareTo(MAX_PRICE) > 0) {
            throw new TripManagementException("INVALID_PRICE_LIMIT", "Giá vé vượt quá mức trần cho phép (10.000.000 VNĐ).");
        }
    }

    public void validateBusSpecification(BusTripFormDto dto) {
        if (dto.getBusType() == null) {
            throw new TripManagementException("INVALID_BUS_TYPE", "Vui lòng chọn loại xe & sơ đồ ghế.");
        }
        String plate = safeTrim(dto.getLicensePlate()).toUpperCase();
        if (plate.length() < 6 || plate.length() > 20 || !LICENSE_PLATE_PATTERN.matcher(plate.replace(" ", "")).matches()) {
            throw new TripManagementException("INVALID_LICENSE_PLATE",
                    "Biển kiểm soát xe không đúng định dạng hợp lệ (VD: 29B-123.45 hoặc 51B-67890).");
        }
    }

    /**
     * Kiểm tra xem xe có bị trùng lịch chạy với một chuyến SCHEDULED khác hay không.
     */
    public boolean hasVehicleScheduleConflict(String licensePlate, LocalDateTime start, LocalDateTime end, Long excludeTripId) {
        if (licensePlate == null || start == null || end == null) {
            return false;
        }
        List<BusTrip> activePlateTrips = tripRepository.findByLicensePlateIgnoreCaseAndStatusNot(
                licensePlate.trim(), TripStatus.CANCELLED);
        for (BusTrip existing : activePlateTrips) {
            if (excludeTripId != null && existing.getId().equals(excludeTripId)) {
                continue;
            }
            if (existing.getDepartureTime() != null && existing.getArrivalTime() != null
                    && start.isBefore(existing.getArrivalTime())
                    && end.isAfter(existing.getDepartureTime())) {
                return true;
            }
        }
        return false;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
