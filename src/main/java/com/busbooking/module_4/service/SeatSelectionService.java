package com.busbooking.module_4.service;

import com.busbooking.common.enums.TripStatus;
import com.busbooking.common.exception.BusinessException;
import com.busbooking.common.exception.ResourceNotFoundException;
import com.busbooking.common.utils.CodeGeneratorUtils;
import com.busbooking.module_1.model.BusTrip;
import com.busbooking.module_1.service.BusTripService;
import com.busbooking.module_2.model.User;
import com.busbooking.module_2.service.AuthAccountService;
import com.busbooking.module_4.dto.SeatHoldResultDto;
import com.busbooking.module_4.enums.SeatStatus;
import com.busbooking.module_4.exception.SeatAlreadyBookedException;
import com.busbooking.module_4.model.Seat;
import com.busbooking.module_4.model.SeatHold;
import com.busbooking.module_4.repository.SeatHoldRepository;
import com.busbooking.module_4.repository.SeatSelectionRepository;
import com.busbooking.module_4.validator.SeatSelectionValidator;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PessimisticLockException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Dịch vụ cốt lõi quản lý giữ ghế, đổi danh sách ghế giữ, giải phóng ghế quá hạn
 * và khóa dữ liệu đồng thời theo thứ tự ID ổn định (Module 4).
 */
@Service
public class SeatSelectionService {

    private static final int MAX_HOLD_TOTAL_MINUTES = 15;
    private static final int DEFAULT_HOLD_MINUTES = 10;

    private final SeatSelectionRepository seatRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final BusTripService busTripService;
    private final AuthAccountService authAccountService;
    private final SeatSelectionValidator seatSelectionValidator;

    @Value("${app.seat-hold.timeout-minutes:10}")
    private int holdTimeoutMinutes;

    public SeatSelectionService(SeatSelectionRepository seatRepository,
                                SeatHoldRepository seatHoldRepository,
                                BusTripService busTripService,
                                AuthAccountService authAccountService,
                                SeatSelectionValidator seatSelectionValidator) {
        this.seatRepository = seatRepository;
        this.seatHoldRepository = seatHoldRepository;
        this.busTripService = busTripService;
        this.authAccountService = authAccountService;
        this.seatSelectionValidator = seatSelectionValidator;
    }

    @Transactional
    public List<Seat> getSeatsForTrip(Long tripId) {
        releaseExpiredHolds();
        return seatRepository.findByBusTripIdOrderByFloorNumberAscRowIndexAscColIndexAsc(tripId);
    }

    /**
     * Giữ ghế tạm thời 10 phút cho người dùng hiện tại.
     * Chống đặt trùng đồng thời bằng PESSIMISTIC_WRITE lock theo thứ tự ID ghế tăng dần + @Version Optimistic Lock.
     * Đảm bảo toàn vẹn giao dịch: nếu bất kỳ ghế nào trong danh sách không hợp lệ sẽ rollback toàn bộ và giữ nguyên phiên cũ.
     */
    @Transactional
    public SeatHoldResultDto holdSeats(Long tripId, List<Long> seatIds, String username) {
        seatSelectionValidator.validateTripOpenForBooking(busTripService.getTripById(tripId));
        List<Long> sortedSeatIds = seatSelectionValidator.normalizeAndSortSeatIds(seatIds);
        BusTrip trip = busTripService.getTripById(tripId);
        if (trip.getStatus() != TripStatus.SCHEDULED) {
            throw new BusinessException("TRIP_NOT_AVAILABLE", "Chuyến xe này không còn mở bán vé.");
        }
        User user = authAccountService.getByUsername(username);

        try {
            List<Seat> lockedTargetSeats = lockTargetSeatsInOrder(trip, sortedSeatIds);
            LocalDateTime now = LocalDateTime.now();

            // Thu thập và kiểm tra toàn bộ ghế đích trước khi đụng tới phiên giữ cũ của người dùng
            Map<Long, List<SeatHold>> holdsBySeatId = new LinkedHashMap<>();
            List<SeatHold> holdsToDeactivate = new ArrayList<>();

            for (Seat seat : lockedTargetSeats) {
                List<SeatHold> activeHolds = seatHoldRepository.findAllBySeatIdAndActiveTrueOrderByIdAsc(seat.getId());
                holdsBySeatId.put(seat.getId(), activeHolds);

                List<SeatHold> validActiveHolds = new ArrayList<>();
                for (SeatHold hold : activeHolds) {
                    if (hold.isExpiredAt(now)) {
                        holdsToDeactivate.add(hold);
                    } else {
                        validActiveHolds.add(hold);
                    }
                }

                validateSeatEligibilityForHold(tripId, user, seat, validActiveHolds);
                // Mọi bản ghi active còn lại trên ghế đích (của chính user khi đổi/chọn lại hoặc đã hết hạn)
                // sẽ được đánh dấu inactive để đảm bảo mỗi ghế chỉ có đúng 1 SeatHold active sau khi lưu.
                holdsToDeactivate.addAll(validActiveHolds);
            }

            // Sau khi toàn bộ ghế đích hợp lệ, xử lý chuyển đổi các phiên giữ cũ của user trên cùng chuyến
            Set<Long> targetIdSet = new HashSet<>(sortedSeatIds);
            List<SeatHold> previousUserHolds = seatHoldRepository.findActiveHoldsByUserAndTrip(user.getId(), tripId);
            Set<Seat> releasedOldSeats = new LinkedHashSet<>();

            for (SeatHold oldHold : previousUserHolds) {
                oldHold.setActive(false);
                Seat oldSeat = oldHold.getSeat();
                if (oldSeat != null && !targetIdSet.contains(oldSeat.getId())) {
                    if (oldSeat.getStatus() == SeatStatus.HELD) {
                        oldSeat.setStatus(SeatStatus.AVAILABLE);
                        releasedOldSeats.add(oldSeat);
                    }
                }
                holdsToDeactivate.add(oldHold);
            }

            if (!holdsToDeactivate.isEmpty()) {
                Set<Long> seenHoldIds = new HashSet<>();
                List<SeatHold> uniqueDeactivations = new ArrayList<>();
                for (SeatHold h : holdsToDeactivate) {
                    if (h.getId() != null && seenHoldIds.add(h.getId())) {
                        h.setActive(false);
                        uniqueDeactivations.add(h);
                    }
                }
                seatHoldRepository.saveAll(uniqueDeactivations);
            }
            if (!releasedOldSeats.isEmpty()) {
                seatRepository.saveAll(releasedOldSeats);
            }
            seatHoldRepository.flush();

            String holdCode = CodeGeneratorUtils.generateHoldCode();
            int baseMinutes = holdTimeoutMinutes > 0 ? holdTimeoutMinutes : DEFAULT_HOLD_MINUTES;
            LocalDateTime expiresAt = now.plusMinutes(baseMinutes);
            LocalDateTime maxExpiresAt = now.plusMinutes(MAX_HOLD_TOTAL_MINUTES);

            BigDecimal estimatedTotal = BigDecimal.ZERO;
            List<SeatHold> newHolds = new ArrayList<>(lockedTargetSeats.size());
            List<Long> confirmedSeatIds = new ArrayList<>(lockedTargetSeats.size());
            List<String> seatNumbers = new ArrayList<>(lockedTargetSeats.size());

            // Sắp xếp lại theo số tầng/hàng/cột khi trả về DTO nhưng khóa DB đã thực hiện theo ID tăng dần
            for (Seat seat : lockedTargetSeats) {
                if (!seat.getStatus().canTransitionTo(SeatStatus.HELD)) {
                    throw SeatAlreadyBookedException.forConcurrentConflict(tripId);
                }
                seat.setStatus(SeatStatus.HELD);
                estimatedTotal = estimatedTotal.add(seat.getPrice());
                newHolds.add(new SeatHold(holdCode, seat, user, now, expiresAt));
            }

            seatRepository.saveAll(lockedTargetSeats);
            seatHoldRepository.saveAll(newHolds);
            seatRepository.flush();
            seatHoldRepository.flush();

            lockedTargetSeats.stream()
                    .sorted(Comparator.comparing(Seat::getFloorNumber)
                            .thenComparing(Seat::getRowIndex)
                            .thenComparing(Seat::getColIndex))
                    .forEach(s -> {
                        confirmedSeatIds.add(s.getId());
                        seatNumbers.add(s.getSeatNumber());
                    });

            busTripService.refreshAvailableSeats(tripId);

            return new SeatHoldResultDto(
                    holdCode,
                    tripId,
                    confirmedSeatIds,
                    seatNumbers,
                    estimatedTotal,
                    now,
                    expiresAt,
                    maxExpiresAt,
                    expiresAt.isBefore(maxExpiresAt),
                    "Đã giữ " + seatNumbers.size() + " ghế (" + String.join(", ", seatNumbers) + ") trong " + baseMinutes + " phút."
            );
        } catch (OptimisticLockException
                 | OptimisticLockingFailureException
                 | PessimisticLockException
                 | PessimisticLockingFailureException
                 | DataIntegrityViolationException ex) {
            throw SeatAlreadyBookedException.forConcurrentConflict(tripId);
        } catch (DataAccessException ex) {
            throw SeatAlreadyBookedException.forConcurrentConflict(tripId);
        }
    }

    /**
     * Đổi danh sách ghế đang giữ từ một mã holdCode hiện có sang danh sách ghế mới trong cùng giao dịch.
     */
    @Transactional
    public SeatHoldResultDto changeHeldSeats(String holdCode, List<Long> newSeatIds, String username) {
        if (holdCode == null || holdCode.isBlank()) {
            throw SeatAlreadyBookedException.forExpiredHold("N/A");
        }
        List<SeatHold> currentHolds = seatHoldRepository.findByHoldCodeOrderByIdAsc(holdCode.trim());
        if (currentHolds.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy phiên giữ ghế với mã: " + holdCode);
        }

        User caller = authAccountService.getByUsername(username);
        SeatHold first = currentHolds.get(0);
        if (!first.isOwnedBy(caller)) {
            throw new BusinessException("UNAUTHORIZED_HOLD", "Bạn không có quyền thay đổi phiên giữ ghế của tài khoản khác.");
        }
        if (first.getSeat().getStatus() == SeatStatus.BOOKED) {
            throw new BusinessException("HOLD_ALREADY_BOOKED", "Phiên giữ ghế đã được chuyển thành vé đặt, không thể đổi ghế.");
        }
        if (!first.isActive() || first.isExpired()) {
            throw SeatAlreadyBookedException.forExpiredHold(holdCode);
        }

        Long tripId = first.getSeat().getBusTrip().getId();
        return holdSeats(tripId, newSeatIds, username);
    }

    @Transactional
    public List<SeatHold> getValidHoldsByCode(String holdCode, String username) {
        releaseExpiredHolds();
        if (holdCode == null || holdCode.isBlank()) {
            throw SeatAlreadyBookedException.forExpiredHold("N/A");
        }
        List<SeatHold> holds = seatHoldRepository.findByHoldCodeAndActiveTrue(holdCode.trim());
        if (holds.isEmpty()) {
            List<SeatHold> historical = seatHoldRepository.findByHoldCodeOrderByIdAsc(holdCode.trim());
            if (!historical.isEmpty() && historical.get(0).getSeat().getStatus() == SeatStatus.BOOKED) {
                throw new BusinessException("HOLD_ALREADY_BOOKED", "Phiên giữ ghế này đã hoàn tất đặt vé trước đó.");
            }
            throw new BusinessException("HOLD_EXPIRED",
                    "Phiên giữ ghế 10 phút đã hết hạn hoặc không tồn tại. Vui lòng chọn lại ghế!");
        }
        User user = authAccountService.getByUsername(username);
        for (SeatHold sh : holds) {
            if (!sh.isOwnedBy(user)) {
                throw new BusinessException("UNAUTHORIZED_HOLD", "Bạn không có quyền truy cập phiên giữ ghế này.");
            }
            if (sh.isExpired()) {
                throw SeatAlreadyBookedException.forExpiredHold(holdCode);
            }
            if (sh.getSeat().getStatus() != SeatStatus.HELD) {
                throw new BusinessException("SEAT_STATE_INVALID",
                        "Trạng thái ghế " + sh.getSeat().getSeatNumber() + " đã thay đổi và không còn giữ chỗ.");
            }
        }
        return holds;
    }

    /**
     * Hủy chủ động một phiên giữ ghế thuộc về người dùng hiện tại.
     * Tuyệt đối không trả ghế đã BOOKED về AVAILABLE.
     */
    @Transactional
    public void releaseHoldByCode(String holdCode, String username) {
        if (holdCode == null || holdCode.isBlank()) {
            throw new BusinessException("INVALID_HOLD_CODE", "Mã giữ ghế không hợp lệ.");
        }
        List<SeatHold> holds = seatHoldRepository.findByHoldCodeOrderByIdAsc(holdCode.trim());
        if (holds.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy phiên giữ ghế với mã: " + holdCode);
        }

        User caller = authAccountService.getByUsername(username);
        for (SeatHold hold : holds) {
            if (!hold.isOwnedBy(caller)) {
                throw new BusinessException("UNAUTHORIZED_HOLD", "Bạn không có quyền hủy phiên giữ ghế của tài khoản khác.");
            }
        }

        Set<Long> tripIds = new LinkedHashSet<>();
        List<Long> heldSeatIds = holds.stream()
                .map(h -> h.getSeat().getId())
                .distinct()
                .sorted()
                .toList();
        if (!heldSeatIds.isEmpty()) {
            seatRepository.findSeatsByIdsWithLock(heldSeatIds);
        }

        for (SeatHold hold : holds) {
            if (hold.isActive()) {
                hold.setActive(false);
            }
            Seat seat = hold.getSeat();
            if (seat != null && seat.getStatus() == SeatStatus.HELD) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seatRepository.save(seat);
            }
            if (seat != null && seat.getBusTrip() != null) {
                tripIds.add(seat.getBusTrip().getId());
            }
        }
        seatHoldRepository.saveAll(holds);
        seatHoldRepository.flush();
        for (Long tripId : tripIds) {
            busTripService.refreshAvailableSeats(tripId);
        }
    }

    /**
     * Tác vụ chạy ngầm định kỳ mỗi 30 giây để giải phóng các ghế giữ quá hạn.
     * Không bao giờ trả ghế BOOKED hoặc ghế đang có phiên giữ mới còn hạn về AVAILABLE.
     */
    @Scheduled(fixedRate = 30000)
    @Transactional
    public int releaseExpiredHolds() {
        LocalDateTime now = LocalDateTime.now();
        List<SeatHold> expiredHolds = seatHoldRepository.findExpiredActiveHolds(now);
        if (expiredHolds.isEmpty()) {
            return 0;
        }

        Set<Long> affectedTrips = new LinkedHashSet<>();
        Set<Long> candidateSeatIds = new LinkedHashSet<>();

        for (SeatHold hold : expiredHolds) {
            hold.setActive(false);
            if (hold.getSeat() != null) {
                candidateSeatIds.add(hold.getSeat().getId());
                if (hold.getSeat().getBusTrip() != null) {
                    affectedTrips.add(hold.getSeat().getBusTrip().getId());
                }
            }
        }
        seatHoldRepository.saveAll(expiredHolds);
        seatHoldRepository.flush();

        if (!candidateSeatIds.isEmpty()) {
            List<Long> sortedIds = candidateSeatIds.stream().sorted().toList();
            List<Seat> lockedSeats = seatRepository.findSeatsByIdsWithLock(sortedIds);
            for (Seat seat : lockedSeats) {
                if (seat.getStatus() == SeatStatus.HELD) {
                    boolean stillHeldByValidSession = seatHoldRepository.existsActiveNonExpiredHoldForSeat(seat.getId(), now);
                    if (!stillHeldByValidSession) {
                        seat.setStatus(SeatStatus.AVAILABLE);
                        seatRepository.save(seat);
                    }
                }
            }
            seatRepository.flush();
        }

        for (Long tripId : affectedTrips) {
            busTripService.refreshAvailableSeats(tripId);
        }
        return expiredHolds.size();
    }

    private List<Seat> lockTargetSeatsInOrder(BusTrip trip, List<Long> sortedSeatIds) {
        List<Seat> lockedSeats = seatRepository.findSeatsForHold(trip.getId(), sortedSeatIds);
        seatSelectionValidator.validateSeatsBelongToTrip(trip, lockedSeats, sortedSeatIds.size());
        return lockedSeats;
    }

    private void validateSeatEligibilityForHold(Long tripId, User user, Seat seat, List<SeatHold> validActiveHolds) {
        SeatStatus status = seat.getStatus();
        if (status == SeatStatus.BOOKED) {
            throw SeatAlreadyBookedException.forBookedSeat(seat.getSeatNumber(), tripId);
        }
        if (status == SeatStatus.UNAVAILABLE) {
            throw SeatAlreadyBookedException.forUnavailableSeat(seat.getSeatNumber(), tripId);
        }
        for (SeatHold validHold : validActiveHolds) {
            if (!validHold.isOwnedBy(user)) {
                throw SeatAlreadyBookedException.forHeldSeat(seat.getSeatNumber(), tripId);
            }
        }
        if (status == SeatStatus.HELD && validActiveHolds.isEmpty()) {
            // Chỉ cho phép tiếp tục nếu trước đó ghế vừa hết hạn (expired holds sẽ được dọn),
            // còn nếu Seat ở trạng thái HELD mà không có bản ghi SeatHold nào thì chặn an toàn.
            List<SeatHold> allActiveFlagged = seatHoldRepository.findAllBySeatIdAndActiveTrueOrderByIdAsc(seat.getId());
            if (allActiveFlagged.isEmpty()) {
                throw SeatAlreadyBookedException.forHeldSeat(seat.getSeatNumber(), tripId);
            }
        }
    }
}
