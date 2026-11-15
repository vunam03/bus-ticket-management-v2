package com.busbooking.module_4.exception;

import com.busbooking.common.exception.BusinessException;

/**
 * Ngoại lệ chuyên biệt cho trường hợp ghế đã được đặt, đang bị giữ, không khả dụng hoặc hết hạn giữ chỗ (Module 4).
 */
public class SeatAlreadyBookedException extends BusinessException {

    private final String seatNumber;
    private final Long tripId;

    public SeatAlreadyBookedException(String errorCode, String message, String seatNumber, Long tripId) {
        super(errorCode, message);
        this.seatNumber = seatNumber;
        this.tripId = tripId;
    }

    public static SeatAlreadyBookedException forBookedSeat(String seatNumber, Long tripId) {
        return new SeatAlreadyBookedException(
                "SEAT_ALREADY_BOOKED",
                "Ghế " + seatNumber + " vừa được hành khách khác đặt thành công. Vui lòng chọn ghế khác!",
                seatNumber,
                tripId
        );
    }

    public static SeatAlreadyBookedException forHeldSeat(String seatNumber, Long tripId) {
        return new SeatAlreadyBookedException(
                "SEAT_BEING_HELD",
                "Ghế " + seatNumber + " vừa được hành khách khác chọn giữ chỗ. Vui lòng chọn ghế khác!",
                seatNumber,
                tripId
        );
    }

    public static SeatAlreadyBookedException forUnavailableSeat(String seatNumber, Long tripId) {
        return new SeatAlreadyBookedException(
                "SEAT_UNAVAILABLE",
                "Ghế " + seatNumber + " hiện đang khóa hoặc không khả dụng. Vui lòng chọn ghế khác!",
                seatNumber,
                tripId
        );
    }

    public static SeatAlreadyBookedException forConcurrentConflict(Long tripId) {
        return new SeatAlreadyBookedException(
                "SEAT_CONCURRENT_CONFLICT",
                "Ghế bạn vừa chọn đang được hành khách khác thao tác đồng thời. Vui lòng kiểm tra lại sơ đồ ghế!",
                null,
                tripId
        );
    }

    public static SeatAlreadyBookedException forExpiredHold(String holdCode) {
        return new SeatAlreadyBookedException(
                "HOLD_EXPIRED",
                "Phiên giữ ghế " + holdCode + " đã hết thời gian hiệu lực. Vui lòng chọn lại ghế trên sơ đồ!",
                null,
                null
        );
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public Long getTripId() {
        return tripId;
    }
}
