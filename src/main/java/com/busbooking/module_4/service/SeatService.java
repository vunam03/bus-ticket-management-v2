package com.busbooking.module_4.service;

import com.busbooking.common.enums.TripStatus;
import com.busbooking.common.exception.BusinessException;
import com.busbooking.common.utils.CurrencyUtils;
import com.busbooking.module_1.model.BusTrip;
import com.busbooking.module_1.service.BusTripService;
import com.busbooking.module_4.dto.SeatHoldResultDto;
import com.busbooking.module_4.dto.SeatLayoutResponse;
import com.busbooking.module_4.dto.SeatStatusDto;
import com.busbooking.module_4.enums.SeatStatus;
import com.busbooking.module_4.exception.SeatAlreadyBookedException;
import com.busbooking.module_4.model.Seat;
import com.busbooking.module_4.model.SeatHold;
import com.busbooking.module_4.repository.SeatRepository;
import com.busbooking.module_4.validator.SeatSelectionValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dịch vụ xây dựng sơ đồ ghế 1-2 tầng, gợi ý chỗ ngồi liền kề thông minh
 * và tính tổng tiền vé theo lựa chọn (Module 4).
 */
@Service
public class SeatService {

    private static final int MAX_SEATS_PER_BOOKING = SeatSelectionValidator.MAX_SEATS_PER_ORDER;

    private final SeatRepository seatRepository;
    private final SeatLockService seatLockService;
    private final BusTripService busTripService;
    private final SeatSelectionValidator seatSelectionValidator;

    public SeatService(SeatRepository seatRepository,
                       SeatLockService seatLockService,
                       BusTripService busTripService,
                       SeatSelectionValidator seatSelectionValidator) {
        this.seatRepository = seatRepository;
        this.seatLockService = seatLockService;
        this.busTripService = busTripService;
        this.seatSelectionValidator = seatSelectionValidator;
    }

    /**
     * Xây dựng dữ liệu sơ đồ ghế đầy đủ kèm thống kê số ghế trống/đang giữ/đã đặt/không khả dụng
     * và thông tin phiên giữ ghế của chính người dùng hiện tại (nếu có).
     */
    @Transactional
    public SeatLayoutResponse getSeatLayoutForTrip(Long tripId, String username) {
        seatLockService.cleanupExpiredLocks();
        BusTrip trip = busTripService.getTripById(tripId);
        List<Seat> seats = seatRepository.findByBusTripIdOrderByFloorNumberAscRowIndexAscColIndexAsc(tripId);
        Map<Long, SeatHold> activeHoldMap = seatLockService.findActiveHoldMapByTrip(tripId);

        List<SeatStatusDto> floor1 = new ArrayList<>();
        List<SeatStatusDto> floor2 = new ArrayList<>();
        int available = 0;
        int held = 0;
        int booked = 0;
        int unavailable = 0;
        int f1Avail = 0;
        int f2Avail = 0;

        for (Seat seat : seats) {
            SeatHold hold = activeHoldMap.get(seat.getId());
            SeatStatusDto dto = SeatStatusDto.fromEntity(seat, hold, username);

            if (seat.getStatus() == SeatStatus.AVAILABLE) {
                available++;
                if (seat.getFloorNumber() != null && seat.getFloorNumber() == 2) {
                    f2Avail++;
                } else {
                    f1Avail++;
                }
            } else if (seat.getStatus() == SeatStatus.HELD) {
                held++;
            } else if (seat.getStatus() == SeatStatus.BOOKED) {
                booked++;
            } else if (seat.getStatus() == SeatStatus.UNAVAILABLE) {
                unavailable++;
            }

            if (seat.getFloorNumber() != null && seat.getFloorNumber() == 2) {
                floor2.add(dto);
            } else {
                floor1.add(dto);
            }
        }

        SeatLayoutResponse response = new SeatLayoutResponse();
        response.setTripId(trip.getId());
        response.setTripCode(trip.getTripCode());
        response.setRouteName(trip.getDepartureProvince() + " → " + trip.getArrivalProvince());
        response.setBusTypeLabel(trip.getBusType() != null ? trip.getBusType().getDisplayName() : "");
        response.setTripBookable(trip.getStatus() == TripStatus.SCHEDULED);
        response.setTripStatusLabel(trip.getStatus() != null ? trip.getStatus().getDisplayName() : "");
        response.setTotalFloors(trip.getBusType() != null ? trip.getBusType().getFloors() : 1);
        response.setTotalSeats(seats.size());
        response.setAvailableCount(available);
        response.setHeldCount(held);
        response.setBookedCount(booked);
        response.setUnavailableCount(unavailable);
        response.setFloor1AvailableCount(f1Avail);
        response.setFloor2AvailableCount(f2Avail);
        response.setUnitPrice(trip.getTicketPrice());
        response.setFormattedUnitPrice(CurrencyUtils.formatVnd(trip.getTicketPrice()));
        response.setFloor1Seats(floor1);
        response.setFloor2Seats(floor2);

        SeatHoldResultDto userActiveHold = seatLockService.getActiveHoldSummaryForUserAndTrip(tripId, username);
        if (userActiveHold != null) {
            LocalDateTime now = LocalDateTime.now();
            response.setActiveHoldCode(userActiveHold.getHoldCode());
            response.setHoldRemainingSeconds(Math.max(0L,
                    Duration.between(now, userActiveHold.getExpiresAt()).getSeconds()));
            response.setCanExtendHold(userActiveHold.isCanExtend());
            if (userActiveHold.getMaxExpiresAt() != null) {
                response.setMaxExtensionRemainingSeconds(Math.max(0L,
                        Duration.between(userActiveHold.getExpiresAt(), userActiveHold.getMaxExpiresAt()).getSeconds()));
            }
            response.setActiveHoldSeatIds(userActiveHold.getSeatIds());
            response.setActiveHoldSeatNumbers(userActiveHold.getSeatNumbers());
        }
        return response;
    }

    /**
     * Lấy danh sách ghế theo số tầng cụ thể.
     */
    @Transactional(readOnly = true)
    public List<SeatStatusDto> getSeatsByFloor(Long tripId, Integer floorNumber, String username) {
        int safeFloor = (floorNumber != null && floorNumber == 2) ? 2 : 1;
        List<Seat> seats = seatRepository.findByBusTripIdAndFloorNumberOrderByRowIndexAscColIndexAsc(tripId, safeFloor);
        Map<Long, SeatHold> activeHoldMap = seatLockService.findActiveHoldMapBySeats(seats);
        return seats.stream()
                .map(seat -> SeatStatusDto.fromEntity(seat, activeHoldMap.get(seat.getId()), username))
                .collect(Collectors.toList());
    }

    /**
     * Kiểm tra tính khả dụng của danh sách ghế và tính tổng tiền dự kiến (tính từ giá gốc DB).
     */
    @Transactional
    public Map<String, Object> calculateSelectionTotal(Long tripId, List<Long> seatIds) {
        return calculateSelectionTotalForUser(tripId, seatIds, null);
    }

    /**
     * Kiểm tra tính khả dụng và tính tổng tiền, cho phép người dùng giữ chỗ tự tính lại ghế mình đang giữ.
     */
    @Transactional
    public Map<String, Object> calculateSelectionTotalForUser(Long tripId, List<Long> seatIds, String username) {
        seatLockService.cleanupExpiredLocks();
        seatSelectionValidator.validateSeatIdList(seatIds);

        BusTrip trip = busTripService.getTripById(tripId);
        seatSelectionValidator.validateTripOpenForBooking(trip);

        List<Seat> tripSeats = seatRepository.findByBusTripIdOrderByFloorNumberAscRowIndexAscColIndexAsc(tripId);
        Map<Long, Seat> seatLookup = tripSeats.stream().collect(Collectors.toMap(Seat::getId, s -> s));
        Map<Long, SeatHold> activeHoldMap = seatLockService.findActiveHoldMapByTrip(tripId);

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal unitPrice = null;
        List<String> seatNumbers = new ArrayList<>();

        for (Long id : seatIds) {
            Seat seat = seatLookup.get(id);
            if (seat == null) {
                throw new BusinessException("SEAT_NOT_IN_TRIP", "Ghế #" + id + " không thuộc chuyến xe này.");
            }
            if (seat.getStatus() == SeatStatus.BOOKED) {
                throw SeatAlreadyBookedException.forBookedSeat(seat.getSeatNumber(), tripId);
            }
            if (seat.getStatus() == SeatStatus.UNAVAILABLE) {
                throw SeatAlreadyBookedException.forUnavailableSeat(seat.getSeatNumber(), tripId);
            }
            if (seat.getStatus() == SeatStatus.HELD) {
                SeatHold hold = activeHoldMap.get(seat.getId());
                if (username == null || hold == null || !hold.isOwnedByUsername(username)) {
                    throw SeatAlreadyBookedException.forHeldSeat(seat.getSeatNumber(), tripId);
                }
            }
            if (unitPrice == null) {
                unitPrice = seat.getPrice();
            }
            totalAmount = totalAmount.add(seat.getPrice());
            seatNumbers.add(seat.getSeatNumber());
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("tripId", tripId);
        summary.put("seatCount", seatNumbers.size());
        summary.put("seatNumbers", seatNumbers);
        summary.put("unitPrice", unitPrice != null ? unitPrice : trip.getTicketPrice());
        summary.put("formattedUnitPrice", CurrencyUtils.formatVnd(unitPrice != null ? unitPrice : trip.getTicketPrice()));
        summary.put("totalAmount", totalAmount);
        summary.put("formattedTotal", CurrencyUtils.formatVnd(totalAmount));
        return summary;
    }

    /**
     * Gợi ý N ghế trống liền kề ưu tiên cùng hàng và cùng tầng (tối đa 6 ghế).
     */
    @Transactional
    @SuppressWarnings("unchecked")
    public List<SeatStatusDto> suggestBestAvailableSeats(Long tripId, int requestedCount, Integer preferredFloor) {
        Map<String, Object> result = suggestSmartSeats(tripId, requestedCount, preferredFloor);
        return (List<SeatStatusDto>) result.get("seats");
    }

    /**
     * Gợi ý N ghế thông minh kèm metadata giải thích kết quả (đủ ghế hay không, liền kề hay ghép cụm).
     */
    @Transactional
    public Map<String, Object> suggestSmartSeats(Long tripId, int requestedCount, Integer preferredFloor) {
        seatLockService.cleanupExpiredLocks();
        if (requestedCount < 1 || requestedCount > MAX_SEATS_PER_BOOKING) {
            throw new BusinessException("MAX_SEAT_LIMIT",
                    "Chỉ hỗ trợ gợi ý từ 1 đến tối đa " + MAX_SEATS_PER_BOOKING + " ghế.");
        }

        BusTrip trip = busTripService.getTripById(tripId);
        seatSelectionValidator.validateTripOpenForBooking(trip);

        Map<Long, SeatHold> activeHoldMap = seatLockService.findActiveHoldMapByTrip(tripId);
        List<Seat> available = seatRepository.findByBusTripIdAndStatusOrderByFloorNumberAscRowIndexAscColIndexAsc(
                        tripId, SeatStatus.AVAILABLE)
                .stream()
                .filter(seat -> seat.getStatus() == SeatStatus.AVAILABLE && !activeHoldMap.containsKey(seat.getId()))
                .toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("tripId", tripId);
        response.put("requestedCount", requestedCount);
        response.put("availableCount", available.size());

        if (available.size() < requestedCount) {
            response.put("sufficient", false);
            response.put("adjacentSameRow", false);
            response.put("sameFloor", false);
            response.put("seats", List.of());
            response.put("message", "Hiện chỉ còn " + available.size() + " ghế trống, không đủ "
                    + requestedCount + " ghế theo yêu cầu.");
            return response;
        }

        int totalFloors = trip.getBusType() != null ? trip.getBusType().getFloors() : 1;
        Integer validFloorPref = (preferredFloor != null && preferredFloor >= 1 && preferredFloor <= totalFloors)
                ? preferredFloor
                : null;

        SuggestionCandidate bestCandidate = findOptimalSeatCluster(available, requestedCount, validFloorPref, totalFloors);
        List<Seat> chosen = bestCandidate.seats();

        List<SeatStatusDto> dtoList = chosen.stream()
                .map(seat -> SeatStatusDto.fromEntity(seat, null, null))
                .toList();
        String seatCodes = chosen.stream().map(Seat::getSeatNumber).collect(Collectors.joining(", "));
        String msg;
        if (bestCandidate.sameRowContiguous()) {
            msg = "Đã chọn " + chosen.size() + " ghế liền kề trên Tầng " + bestCandidate.primaryFloor() + ": " + seatCodes;
        } else if (bestCandidate.sameFloor()) {
            msg = "Đã chọn " + chosen.size() + " ghế gần nhau trên Tầng " + bestCandidate.primaryFloor() + ": " + seatCodes;
        } else {
            msg = "Không đủ ghế trống trên 1 tầng, hệ thống gợi ý cụm ghế gần nhất trên cả 2 tầng: " + seatCodes;
        }

        response.put("sufficient", true);
        response.put("adjacentSameRow", bestCandidate.sameRowContiguous());
        response.put("sameFloor", bestCandidate.sameFloor());
        response.put("floorUsed", bestCandidate.sameFloor() ? bestCandidate.primaryFloor() : null);
        response.put("seats", dtoList);
        response.put("seatIds", chosen.stream().map(Seat::getId).toList());
        response.put("seatNumbers", chosen.stream().map(Seat::getSeatNumber).toList());
        response.put("message", msg);
        return response;
    }

    private SuggestionCandidate findOptimalSeatCluster(List<Seat> available,
                                                       int requestedCount,
                                                       Integer preferredFloor,
                                                       int totalFloors) {
        Map<Integer, List<Seat>> byFloor = available.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getFloorNumber() != null ? s.getFloorNumber() : 1,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<Integer> floorOrder = new ArrayList<>();
        if (preferredFloor != null) {
            floorOrder.add(preferredFloor);
            for (int f = 1; f <= totalFloors; f++) {
                if (f != preferredFloor) {
                    floorOrder.add(f);
                }
            }
        } else {
            for (int f = 1; f <= totalFloors; f++) {
                floorOrder.add(f);
            }
        }

        // Nếu người dùng không chỉ định tầng, chọn phương án có điểm gom cụm tốt nhất giữa các tầng;
        // nếu người dùng chỉ định tầng và tầng đó đủ ghế, ưu tiên hoàn toàn tầng mong muốn.
        SuggestionCandidate best = null;
        for (Integer floor : floorOrder) {
            List<Seat> floorSeats = byFloor.getOrDefault(floor, List.of());
            if (floorSeats.size() < requestedCount) {
                continue;
            }
            SuggestionCandidate candidate = findBestClusterOnSingleFloor(floor, floorSeats, requestedCount);
            if (preferredFloor != null && floor.equals(preferredFloor)) {
                return candidate;
            }
            if (best == null || candidate.penaltyScore() < best.penaltyScore()) {
                best = candidate;
            }
        }
        if (best != null) {
            return best;
        }

        // Không tầng nào đủ toàn bộ N ghế -> lấy cụm nhiều ghế nhất từ tầng ưu tiên rồi bù từ tầng còn lại
        List<Seat> combined = new ArrayList<>();
        for (Integer floor : floorOrder) {
            List<Seat> floorSeats = byFloor.getOrDefault(floor, List.of());
            for (Seat s : floorSeats) {
                if (combined.size() < requestedCount) {
                    combined.add(s);
                }
            }
        }
        return new SuggestionCandidate(combined, false, false, floorOrder.get(0), 10_000);
    }

    private SuggestionCandidate findBestClusterOnSingleFloor(int floor, List<Seat> floorSeats, int count) {
        List<Seat> sorted = floorSeats.stream()
                .sorted(Comparator.comparingInt((Seat s) -> s.getRowIndex() != null ? s.getRowIndex() : 99)
                        .thenComparingInt(s -> s.getColIndex() != null ? s.getColIndex() : 99))
                .toList();

        if (count == 1) {
            return new SuggestionCandidate(List.of(sorted.get(0)), true, true, floor,
                    sorted.get(0).getRowIndex() != null ? sorted.get(0).getRowIndex() : 1);
        }

        Map<Integer, List<Seat>> byRow = sorted.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getRowIndex() != null ? s.getRowIndex() : 1,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // Ưu tiên 1: N ghế liền kề trên cùng một hàng
        SuggestionCandidate bestSameRow = null;
        for (Map.Entry<Integer, List<Seat>> entry : byRow.entrySet()) {
            List<Seat> rowSeats = entry.getValue();
            if (rowSeats.size() < count) {
                continue;
            }
            for (int i = 0; i <= rowSeats.size() - count; i++) {
                List<Seat> window = rowSeats.subList(i, i + count);
                boolean contiguous = isContiguousColumns(window);
                int colSpan = colAt(window.get(window.size() - 1)) - colAt(window.get(0));
                int score = (contiguous ? 0 : 100) + colSpan * 5 + entry.getKey();
                if (bestSameRow == null || score < bestSameRow.penaltyScore()) {
                    bestSameRow = new SuggestionCandidate(List.copyOf(window), contiguous, true, floor, score);
                }
            }
        }
        if (bestSameRow != null) {
            return bestSameRow;
        }

        // Ưu tiên 2: Cụm N ghế gần nhau nhất qua các hàng liền kề trên cùng một tầng
        SuggestionCandidate bestCluster = null;
        for (int i = 0; i <= sorted.size() - count; i++) {
            List<Seat> window = sorted.subList(i, i + count);
            int minRow = window.stream().mapToInt(s -> s.getRowIndex() != null ? s.getRowIndex() : 1).min().orElse(1);
            int maxRow = window.stream().mapToInt(s -> s.getRowIndex() != null ? s.getRowIndex() : 1).max().orElse(1);
            int minCol = window.stream().mapToInt(this::colAt).min().orElse(1);
            int maxCol = window.stream().mapToInt(this::colAt).max().orElse(1);
            int score = 1000 + (maxRow - minRow) * 40 + (maxCol - minCol) * 5 + minRow;
            if (bestCluster == null || score < bestCluster.penaltyScore()) {
                bestCluster = new SuggestionCandidate(List.copyOf(window), false, true, floor, score);
            }
        }
        return bestCluster != null ? bestCluster
                : new SuggestionCandidate(sorted.subList(0, count), false, true, floor, 5000);
    }

    private boolean isContiguousColumns(List<Seat> seats) {
        for (int i = 0; i < seats.size() - 1; i++) {
            if (colAt(seats.get(i + 1)) - colAt(seats.get(i)) != 1) {
                return false;
            }
        }
        return true;
    }

    private int colAt(Seat seat) {
        return seat.getColIndex() != null ? seat.getColIndex() : 99;
    }

    private record SuggestionCandidate(
            List<Seat> seats,
            boolean sameRowContiguous,
            boolean sameFloor,
            int primaryFloor,
            int penaltyScore
    ) {
    }
}
