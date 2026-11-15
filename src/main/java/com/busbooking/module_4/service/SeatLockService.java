package com.busbooking.module_4.service;

import com.busbooking.common.exception.BusinessException;
import com.busbooking.common.exception.ResourceNotFoundException;
import com.busbooking.module_2.model.User;
import com.busbooking.module_2.service.AuthAccountService;
import com.busbooking.module_4.dto.SeatHoldResultDto;
import com.busbooking.module_4.enums.SeatStatus;
import com.busbooking.module_4.exception.SeatAlreadyBookedException;
import com.busbooking.module_4.model.Seat;
import com.busbooking.module_4.model.SeatHold;
import com.busbooking.module_4.repository.SeatHoldRepository;
import com.busbooking.module_4.repository.SeatSelectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Dịch vụ quản lý khóa giữ ghế tạm thời, gia hạn giữ chỗ (mặc định 10 phút, tối đa 15 phút)
 * và đếm ngược thời gian còn lại (Module 4).
 */
@Service
public class SeatLockService {

    public static final int DEFAULT_HOLD_MINUTES = 10;
    public static final int MAX_HOLD_TOTAL_MINUTES = 15;
    public static final int DEFAULT_EXTEND_MINUTES = 3;

    private final SeatHoldRepository seatHoldRepository;
    private final SeatSelectionRepository seatSelectionRepository;
    private final SeatSelectionService seatSelectionService;
    private final AuthAccountService authAccountService;

    public SeatLockService(SeatHoldRepository seatHoldRepository,
                           SeatSelectionRepository seatSelectionRepository,
                           SeatSelectionService seatSelectionService,
                           AuthAccountService authAccountService) {
        this.seatHoldRepository = seatHoldRepository;
        this.seatSelectionRepository = seatSelectionRepository;
        this.seatSelectionService = seatSelectionService;
        this.authAccountService = authAccountService;
    }

    /**
     * Thu hồi toàn bộ ghế giữ quá hạn qua SeatSelectionService để đồng bộ trạng thái trống.
     */
    @Transactional
    public int cleanupExpiredLocks() {
        return seatSelectionService.releaseExpiredHolds();
    }

    /**
     * Kiểm tra quyền sở hữu và thời hạn hiệu lực của mã giữ chỗ.
     */
    @Transactional
    public List<SeatHold> validateHoldOwnership(String holdCode, String username) {
        cleanupExpiredLocks();
        if (holdCode == null || holdCode.isBlank()) {
            throw SeatAlreadyBookedException.forExpiredHold("N/A");
        }
        String trimmedCode = holdCode.trim();
        List<SeatHold> holds = seatHoldRepository.findByHoldCodeAndActiveTrue(trimmedCode);
        if (holds.isEmpty()) {
            List<SeatHold> historical = seatHoldRepository.findByHoldCodeOrderByIdAsc(trimmedCode);
            if (historical.isEmpty()) {
                throw new ResourceNotFoundException("Không tìm thấy phiên giữ ghế với mã: " + trimmedCode);
            }
            if (historical.stream().anyMatch(h -> h.getSeat() != null && h.getSeat().getStatus() == SeatStatus.BOOKED)) {
                throw new BusinessException("HOLD_ALREADY_BOOKED",
                        "Phiên giữ ghế " + trimmedCode + " đã được chuyển thành vé đặt thành công.");
            }
            throw SeatAlreadyBookedException.forExpiredHold(trimmedCode);
        }

        User currentUser = authAccountService.getByUsername(username);
        for (SeatHold hold : holds) {
            if (!hold.isOwnedBy(currentUser)) {
                throw new BusinessException("UNAUTHORIZED_HOLD",
                        "Bạn không có quyền thao tác với phiên giữ ghế của tài khoản khác.");
            }
            if (hold.getSeat().getStatus() == SeatStatus.BOOKED) {
                throw new BusinessException("HOLD_ALREADY_BOOKED",
                        "Phiên giữ ghế " + trimmedCode + " đã được thanh toán và xuất vé.");
            }
            if (hold.isExpired()) {
                throw SeatAlreadyBookedException.forExpiredHold(trimmedCode);
            }
        }
        return holds;
    }

    /**
     * Trả về thông tin chi tiết thời hạn giữ ghế của chủ sở hữu.
     */
    @Transactional
    public Map<String, Object> getHoldStatusSummary(String holdCode, String username) {
        List<SeatHold> holds = validateHoldOwnership(holdCode, username);
        SeatHold first = holds.get(0);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxAllowedExpiry = first.getHeldAt().plusMinutes(MAX_HOLD_TOTAL_MINUTES);
        long remainingSeconds = Math.max(0L, Duration.between(now, first.getExpiresAt()).getSeconds());
        long maxExtensionRemainingSeconds = Math.max(0L, Duration.between(first.getExpiresAt(), maxAllowedExpiry).getSeconds());
        boolean canExtend = first.getExpiresAt().isBefore(maxAllowedExpiry);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("holdCode", first.getHoldCode());
        summary.put("tripId", first.getSeat().getBusTrip().getId());
        summary.put("active", remainingSeconds > 0);
        summary.put("heldAt", first.getHeldAt());
        summary.put("expiresAt", first.getExpiresAt());
        summary.put("maxExpiresAt", maxAllowedExpiry);
        summary.put("remainingSeconds", remainingSeconds);
        summary.put("canExtend", canExtend);
        summary.put("maxExtensionRemainingSeconds", maxExtensionRemainingSeconds);
        summary.put("seatNumbers", holds.stream().map(h -> h.getSeat().getSeatNumber()).toList());
        return summary;
    }

    /**
     * Trả về số giây còn lại của phiên giữ ghế đang hoạt động.
     */
    @Transactional
    public long getRemainingHoldSeconds(String holdCode, String username) {
        List<SeatHold> holds = validateHoldOwnership(holdCode, username);
        LocalDateTime earliestExpiry = holds.get(0).getExpiresAt();
        return Math.max(0L, Duration.between(LocalDateTime.now(), earliestExpiry).getSeconds());
    }

    /**
     * Gia hạn thêm tối đa extraMinutes cho phiên giữ chỗ (không vượt quá 15 phút kể từ lúc giữ).
     * Nếu phiên đã đạt trần 15 phút, từ chối và không cộng thêm thời gian.
     */
    @Transactional
    public SeatHoldResultDto extendHold(String holdCode, String username, int extraMinutes) {
        if (extraMinutes <= 0) {
            throw new BusinessException("INVALID_EXTEND_MINUTES", "Số phút gia hạn phải lớn hơn 0.");
        }
        List<SeatHold> holds = validateHoldOwnership(holdCode, username);

        // Khóa ghi các ghế thuộc phiên giữ theo ID tăng dần để đồng bộ với luồng đặt vé/giải phóng
        List<Long> seatIdsSorted = holds.stream()
                .map(h -> h.getSeat().getId())
                .distinct()
                .sorted()
                .toList();
        Long tripId = holds.get(0).getSeat().getBusTrip().getId();
        seatSelectionRepository.findSeatsForHold(tripId, seatIdsSorted);

        LocalDateTime now = LocalDateTime.now();
        SeatHold first = holds.get(0);
        if (first.isExpiredAt(now)) {
            throw SeatAlreadyBookedException.forExpiredHold(holdCode);
        }

        LocalDateTime maxAllowedExpiry = first.getHeldAt().plusMinutes(MAX_HOLD_TOTAL_MINUTES);
        if (!first.getExpiresAt().isBefore(maxAllowedExpiry)) {
            throw new BusinessException("HOLD_MAX_LIMIT_REACHED",
                    "Phiên giữ ghế đã đạt thời gian tối đa 15 phút kể từ lúc giữ chỗ, không thể gia hạn thêm.");
        }

        int boundedExtra = Math.min(extraMinutes, 5);
        LocalDateTime candidateExpiry = first.getExpiresAt().plusMinutes(boundedExtra);
        boolean reachedCapNow = !candidateExpiry.isBefore(maxAllowedExpiry);
        LocalDateTime finalExpiry = candidateExpiry.isAfter(maxAllowedExpiry) ? maxAllowedExpiry : candidateExpiry;

        BigDecimal total = BigDecimal.ZERO;
        List<Long> seatIds = new ArrayList<>();
        List<String> seatNumbers = new ArrayList<>();

        for (SeatHold hold : holds) {
            hold.setExpiresAt(finalExpiry);
            Seat seat = hold.getSeat();
            seatIds.add(seat.getId());
            seatNumbers.add(seat.getSeatNumber());
            total = total.add(seat.getPrice());
        }
        seatHoldRepository.saveAll(holds);
        seatHoldRepository.flush();

        boolean canExtendFurther = finalExpiry.isBefore(maxAllowedExpiry);
        long addedSeconds = Math.max(0L, Duration.between(now, finalExpiry).getSeconds());
        String msg = reachedCapNow
                ? "Đã gia hạn giữ ghế đến mức tối đa 15 phút (còn " + (addedSeconds / 60) + " phút " + (addedSeconds % 60) + " giây)."
                : "Gia hạn giữ ghế thành công thêm " + boundedExtra + " phút (còn " + (addedSeconds / 60) + " phút " + (addedSeconds % 60) + " giây).";

        return new SeatHoldResultDto(
                first.getHoldCode(),
                tripId,
                seatIds,
                seatNumbers,
                total,
                first.getHeldAt(),
                finalExpiry,
                maxAllowedExpiry,
                canExtendFurther,
                msg
        );
    }

    /**
     * Tìm phiên giữ ghế đang còn hiệu lực của chính người dùng trên một chuyến xe cụ thể.
     */
    @Transactional(readOnly = true)
    public SeatHoldResultDto getActiveHoldSummaryForUserAndTrip(Long tripId, String username) {
        if (tripId == null || username == null || username.isBlank() || "anonymousUser".equalsIgnoreCase(username)) {
            return null;
        }
        User user;
        try {
            user = authAccountService.getByUsername(username);
        } catch (ResourceNotFoundException ex) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        List<SeatHold> activeHolds = seatHoldRepository.findActiveHoldsByUserAndTrip(user.getId(), tripId).stream()
                .filter(hold -> hold.isActive() && !hold.isExpiredAt(now))
                .toList();
        if (activeHolds.isEmpty()) {
            return null;
        }

        SeatHold first = activeHolds.get(0);
        LocalDateTime maxExpiresAt = first.getHeldAt().plusMinutes(MAX_HOLD_TOTAL_MINUTES);
        BigDecimal total = BigDecimal.ZERO;
        List<Long> seatIds = new ArrayList<>();
        List<String> seatNumbers = new ArrayList<>();

        for (SeatHold hold : activeHolds) {
            Seat seat = hold.getSeat();
            seatIds.add(seat.getId());
            seatNumbers.add(seat.getSeatNumber());
            total = total.add(seat.getPrice());
        }
        return new SeatHoldResultDto(
                first.getHoldCode(),
                tripId,
                seatIds,
                seatNumbers,
                total,
                first.getHeldAt(),
                first.getExpiresAt(),
                maxExpiresAt,
                first.getExpiresAt().isBefore(maxExpiresAt),
                null
        );
    }

    /**
     * Tạo bản đồ ánh xạ Seat ID sang bản ghi SeatHold đang hoạt động (truy vấn 1 lần theo chuyến xe để tối ưu hiệu năng).
     */
    @Transactional(readOnly = true)
    public Map<Long, SeatHold> findActiveHoldMapByTrip(Long tripId) {
        Map<Long, SeatHold> result = new HashMap<>();
        if (tripId == null) {
            return result;
        }
        LocalDateTime now = LocalDateTime.now();
        List<SeatHold> activeHolds = seatHoldRepository.findActiveHoldsByTrip(tripId);
        for (SeatHold hold : activeHolds) {
            if (hold.isActive() && !hold.isExpiredAt(now) && hold.getSeat() != null) {
                result.putIfAbsent(hold.getSeat().getId(), hold);
            }
        }
        return result;
    }

    /**
     * Tạo bản đồ ánh xạ Seat ID sang bản ghi SeatHold đang hoạt động để phục vụ hiển thị sơ đồ ghế.
     */
    @Transactional(readOnly = true)
    public Map<Long, SeatHold> findActiveHoldMapBySeats(List<Seat> seats) {
        Map<Long, SeatHold> result = new HashMap<>();
        if (seats == null || seats.isEmpty()) {
            return result;
        }
        Long tripId = seats.get(0).getBusTrip() != null ? seats.get(0).getBusTrip().getId() : null;
        if (tripId != null) {
            return findActiveHoldMapByTrip(tripId);
        }
        LocalDateTime now = LocalDateTime.now();
        for (Seat seat : seats) {
            seatHoldRepository.findBySeatIdAndActiveTrue(seat.getId())
                    .filter(hold -> !hold.isExpiredAt(now))
                    .ifPresent(hold -> result.put(seat.getId(), hold));
        }
        return result;
    }
}
