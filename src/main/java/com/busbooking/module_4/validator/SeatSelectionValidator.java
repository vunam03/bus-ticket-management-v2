package com.busbooking.module_4.validator;

import com.busbooking.common.enums.TripStatus;
import com.busbooking.common.exception.BusinessException;
import com.busbooking.module_1.model.BusTrip;
import com.busbooking.module_4.dto.SeatSelectRequest;
import com.busbooking.module_4.enums.SeatStatus;
import com.busbooking.module_4.exception.SeatAlreadyBookedException;
import com.busbooking.module_4.model.Seat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Validator kiểm tra quy tắc nghiệp vụ chọn ghế, giới hạn tối đa 6 ghế và thứ tự khóa ghế ổn định (Module 4).
 */
@Component
public class SeatSelectionValidator {

    public static final int MAX_SEATS_PER_ORDER = 6;

    /**
     * Kiểm tra điều kiện đầu vào của yêu cầu chọn/tính tiền ghế.
     */
    public void validateSelectionRequest(SeatSelectRequest request) {
        if (request == null || request.getTripId() == null) {
            throw new BusinessException("INVALID_TRIP_SELECTION", "Thiếu thông tin mã chuyến xe khi chọn ghế.");
        }
        validateSeatIdList(request.getSeatIds());
    }

    /**
     * Kiểm tra danh sách ID ghế: không rỗng, không chứa null, không trùng lặp và không vượt quá 6 ghế.
     */
    public void validateSeatIdList(List<Long> seatIds) {
        if (seatIds == null || seatIds.isEmpty()) {
            throw new BusinessException("EMPTY_SEAT_SELECTION", "Vui lòng chọn ít nhất 1 ghế trước khi tiếp tục.");
        }
        if (seatIds.stream().anyMatch(Objects::isNull)) {
            throw new BusinessException("INVALID_SEATS", "Danh sách ghế chứa mã ghế không hợp lệ.");
        }
        Set<Long> uniqueIds = new LinkedHashSet<>(seatIds);
        if (uniqueIds.size() != seatIds.size()) {
            throw new BusinessException("DUPLICATE_SEAT_ID", "Danh sách ghế chọn chứa mã ghế bị trùng lặp.");
        }
        if (uniqueIds.size() > MAX_SEATS_PER_ORDER) {
            throw new BusinessException("MAX_SEAT_LIMIT",
                    "Mỗi lượt giao dịch chỉ được chọn tối đa " + MAX_SEATS_PER_ORDER + " ghế.");
        }
    }

    /**
     * Chuẩn hóa và sắp xếp ID ghế theo thứ tự tăng dần để tất cả giao dịch khóa DB theo cùng một thứ tự.
     */
    public List<Long> normalizeAndSortSeatIds(List<Long> seatIds) {
        validateSeatIdList(seatIds);
        List<Long> sorted = new ArrayList<>(seatIds);
        Collections.sort(sorted);
        return sorted;
    }

    /**
     * Kiểm tra trạng thái chuyến xe còn mở bán hay không.
     */
    public void validateTripOpenForBooking(BusTrip trip) {
        if (trip == null || trip.getStatus() != TripStatus.SCHEDULED) {
            throw new BusinessException("TRIP_NOT_AVAILABLE", "Chuyến xe này hiện không còn mở bán vé trực tuyến.");
        }
    }

    /**
     * Kiểm tra danh sách ghế đã khóa có đầy đủ và thuộc đúng chuyến xe hay không.
     */
    public void validateSeatsBelongToTrip(BusTrip trip, List<Seat> seats, int expectedCount) {
        validateTripOpenForBooking(trip);
        if (seats == null || seats.size() != expectedCount) {
            throw new BusinessException("SEAT_NOT_IN_TRIP",
                    "Có ghế không tồn tại hoặc không thuộc chuyến xe đã chọn.");
        }
        for (Seat seat : seats) {
            if (seat.getBusTrip() == null || !seat.getBusTrip().getId().equals(trip.getId())) {
                throw new BusinessException("SEAT_TRIP_MISMATCH",
                        "Ghế " + seat.getSeatNumber() + " không thuộc chuyến xe đã chọn.");
            }
        }
    }

    /**
     * Kiểm tra toàn bộ danh sách ghế sẵn sàng cho thao tác tính tiền nhanh (chưa giữ, chưa bán).
     */
    public void validateTripAndSeatsAvailable(BusTrip trip, List<Seat> seats, int expectedCount) {
        validateSeatsBelongToTrip(trip, seats, expectedCount);
        for (Seat seat : seats) {
            if (seat.getStatus() == SeatStatus.BOOKED) {
                throw SeatAlreadyBookedException.forBookedSeat(seat.getSeatNumber(), trip.getId());
            }
            if (seat.getStatus() == SeatStatus.UNAVAILABLE) {
                throw SeatAlreadyBookedException.forUnavailableSeat(seat.getSeatNumber(), trip.getId());
            }
            if (seat.getStatus() == SeatStatus.HELD) {
                throw SeatAlreadyBookedException.forHeldSeat(seat.getSeatNumber(), trip.getId());
            }
        }
    }
}
