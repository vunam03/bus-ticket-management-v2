package com.busbooking.module_4.service;

import com.busbooking.module_1.service.BusTripService;
import com.busbooking.module_2.service.AuthAccountService;
import com.busbooking.module_4.dto.SeatHoldResultDto;
import com.busbooking.module_4.repository.SeatHoldRepository;
import com.busbooking.module_4.repository.SeatSelectionRepository;
import com.busbooking.shared.enums.SeatStatus;
import com.busbooking.shared.enums.TripStatus;
import com.busbooking.shared.exception.BusinessException;
import com.busbooking.shared.model.BusTrip;
import com.busbooking.shared.model.Seat;
import com.busbooking.shared.model.SeatHold;
import com.busbooking.shared.model.User;
import com.busbooking.shared.utils.CodeGeneratorUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeatSelectionService {

    private final SeatSelectionRepository seatRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final BusTripService busTripService;
    private final AuthAccountService authAccountService;

    @Value("${app.seat-hold.timeout-minutes:10}")
    private int holdTimeoutMinutes;

    public SeatSelectionService(SeatSelectionRepository seatRepository,
                                SeatHoldRepository seatHoldRepository,
                                BusTripService busTripService,
                                AuthAccountService authAccountService) {
        this.seatRepository = seatRepository;
        this.seatHoldRepository = seatHoldRepository;
        this.busTripService = busTripService;
        this.authAccountService = authAccountService;
    }

    @Transactional
    public List<Seat> getSeatsForTrip(Long tripId) {
        releaseExpiredHolds();
        return seatRepository.findByBusTripIdOrderByFloorNumberAscRowIndexAscColIndexAsc(tripId);
    }

    /**
     * Giữ ghế tạm thời 10 phút cho người dùng hiện tại.
     * Chống 2 người đặt trùng ghế cùng lúc bằng kiểm tra trạng thái + Optimistic Lock (@Version).
     */
    @Transactional
    public SeatHoldResultDto holdSeats(Long tripId, List<Long> seatIds, String username) {
        releaseExpiredHolds();

        if (seatIds == null || seatIds.isEmpty()) {
            throw new BusinessException("EMPTY_SEAT_SELECTION", "Vui lòng chọn ít nhất 1 ghế trước khi tiếp tục.");
        }
        if (seatIds.size() > 6) {
            throw new BusinessException("MAX_SEAT_LIMIT", "Mỗi lần đặt chỉ được chọn tối đa 6 ghế.");
        }

        BusTrip trip = busTripService.getTripById(tripId);
        if (trip.getStatus() != TripStatus.SCHEDULED) {
            throw new BusinessException("TRIP_NOT_AVAILABLE", "Chuyến xe này không còn mở bán vé.");
        }

        User user = authAccountService.getByUsername(username);
        List<Seat> seats = seatRepository.findSeatsForHold(tripId, seatIds);
        if (seats.size() != seatIds.size()) {
            throw new BusinessException("INVALID_SEATS", "Một hoặc nhiều ghế bạn chọn không hợp lệ.");
        }

        // Kiểm tra chống đặt trùng ghế
        for (Seat seat : seats) {
            if (seat.getStatus() == SeatStatus.BOOKED) {
                throw new BusinessException("SEAT_ALREADY_BOOKED",
                        "Ghế " + seat.getSeatNumber() + " vừa được khách hàng khác đặt thành công. Vui lòng chọn ghế khác!");
            }
            if (seat.getStatus() == SeatStatus.HELD) {
                Optional<SeatHold> activeHold = seatHoldRepository.findBySeatIdAndActiveTrue(seat.getId());
                if (activeHold.isPresent() && !activeHold.get().getUser().getId().equals(user.getId())) {
                    throw new BusinessException("SEAT_BEING_HELD",
                            "Ghế " + seat.getSeatNumber() + " đang được khách hàng khác giữ chỗ tạm thời. Vui lòng chọn ghế khác!");
                }
            }
        }

        // Giải phóng các hold cũ của chính user này trên cùng chuyến xe trước khi tạo hold mới
        List<SeatHold> previousUserHolds = seatHoldRepository.findActiveHoldsByUserAndTrip(user.getId(), tripId);
        for (SeatHold oldHold : previousUserHolds) {
            oldHold.setActive(false);
            if (oldHold.getSeat().getStatus() == SeatStatus.HELD) {
                oldHold.getSeat().setStatus(SeatStatus.AVAILABLE);
            }
        }
        seatHoldRepository.saveAll(previousUserHolds);

        String holdCode = CodeGeneratorUtils.generateHoldCode();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(holdTimeoutMinutes > 0 ? holdTimeoutMinutes : 10);

        BigDecimal estimatedTotal = BigDecimal.ZERO;
        List<SeatHold> newHolds = new ArrayList<>();
        List<String> seatNumbers = new ArrayList<>();

        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.HELD);
            estimatedTotal = estimatedTotal.add(seat.getPrice());
            seatNumbers.add(seat.getSeatNumber());
            newHolds.add(new SeatHold(holdCode, seat, user, now, expiresAt));
        }

        seatRepository.saveAll(seats);
        seatHoldRepository.saveAll(newHolds);
        busTripService.refreshAvailableSeats(tripId);

        return new SeatHoldResultDto(
                holdCode,
                tripId,
                seats.stream().map(Seat::getId).collect(Collectors.toList()),
                seatNumbers,
                estimatedTotal,
                expiresAt
        );
    }

    @Transactional
    public List<SeatHold> getValidHoldsByCode(String holdCode, String username) {
        releaseExpiredHolds();
        List<SeatHold> holds = seatHoldRepository.findByHoldCodeAndActiveTrue(holdCode);
        if (holds.isEmpty()) {
            throw new BusinessException("HOLD_EXPIRED", "Phiên giữ ghế 10 phút đã hết hạn hoặc không tồn tại. Vui lòng chọn lại ghế!");
        }
        User user = authAccountService.getByUsername(username);
        for (SeatHold sh : holds) {
            if (!sh.getUser().getId().equals(user.getId())) {
                throw new BusinessException("UNAUTHORIZED_HOLD", "Bạn không có quyền truy cập phiên giữ ghế này.");
            }
        }
        return holds;
    }

    @Transactional
    public void releaseHoldByCode(String holdCode, String username) {
        List<SeatHold> holds = seatHoldRepository.findByHoldCodeAndActiveTrue(holdCode);
        if (holds.isEmpty()) {
            return;
        }
        User user = authAccountService.getByUsername(username);
        Set<Long> tripIds = new HashSet<>();
        for (SeatHold hold : holds) {
            if (hold.getUser().getId().equals(user.getId())) {
                hold.setActive(false);
                Seat seat = hold.getSeat();
                if (seat.getStatus() == SeatStatus.HELD) {
                    seat.setStatus(SeatStatus.AVAILABLE);
                    seatRepository.save(seat);
                }
                tripIds.add(seat.getBusTrip().getId());
            }
        }
        seatHoldRepository.saveAll(holds);
        for (Long tripId : tripIds) {
            busTripService.refreshAvailableSeats(tripId);
        }
    }

    /**
     * Tác vụ chạy ngầm định kỳ mỗi 30 giây để giải phóng các ghế giữ quá hạn 10 phút.
     */
    @Scheduled(fixedRate = 30000)
    @Transactional
    public int releaseExpiredHolds() {
        List<SeatHold> expiredHolds = seatHoldRepository.findExpiredActiveHolds(LocalDateTime.now());
        if (expiredHolds.isEmpty()) {
            return 0;
        }
        Set<Long> affectedTrips = new HashSet<>();
        for (SeatHold hold : expiredHolds) {
            hold.setActive(false);
            Seat seat = hold.getSeat();
            if (seat.getStatus() == SeatStatus.HELD) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seatRepository.save(seat);
            }
            affectedTrips.add(seat.getBusTrip().getId());
        }
        seatHoldRepository.saveAll(expiredHolds);
        for (Long tripId : affectedTrips) {
            busTripService.refreshAvailableSeats(tripId);
        }
        return expiredHolds.size();
    }
}
