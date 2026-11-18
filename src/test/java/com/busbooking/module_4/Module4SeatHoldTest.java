package com.busbooking.module_4;

import com.busbooking.common.enums.PaymentMethod;
import com.busbooking.common.enums.PaymentStatus;
import com.busbooking.common.enums.TicketStatus;
import com.busbooking.common.exception.BusinessException;
import com.busbooking.module_1.model.BusTrip;
import com.busbooking.module_1.service.BusTripService;
import com.busbooking.module_4.dto.SeatHoldResultDto;
import com.busbooking.module_4.dto.SeatStatusDto;
import com.busbooking.module_4.enums.SeatStatus;
import com.busbooking.module_4.exception.SeatAlreadyBookedException;
import com.busbooking.module_4.model.Seat;
import com.busbooking.module_4.model.SeatHold;
import com.busbooking.module_4.repository.SeatHoldRepository;
import com.busbooking.module_4.repository.SeatRepository;
import com.busbooking.module_4.service.SeatLockService;
import com.busbooking.module_4.service.SeatSelectionService;
import com.busbooking.module_4.service.SeatService;
import com.busbooking.module_5.dto.BookingCheckoutDto;
import com.busbooking.module_5.model.Ticket;
import com.busbooking.module_5.service.TicketBookingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class Module4SeatHoldTest {

    @Autowired
    private SeatSelectionService seatSelectionService;

    @Autowired
    private SeatLockService seatLockService;

    @Autowired
    private SeatService seatService;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private SeatHoldRepository seatHoldRepository;

    @Autowired
    private BusTripService busTripService;

    @Autowired
    private TicketBookingService ticketBookingService;

    private List<Seat> findAvailableSeats(Long tripId, int minRequired) {
        List<Seat> available = seatSelectionService.getSeatsForTrip(tripId).stream()
                .filter(s -> s.getStatus() == SeatStatus.AVAILABLE)
                .toList();
        assertTrue(available.size() >= minRequired, "Chuyến xe cần tối thiểu " + minRequired + " ghế trống để kiểm thử.");
        return available;
    }

    private void safeReleaseHold(String holdCode, String username) {
        if (holdCode == null) {
            return;
        }
        try {
            seatSelectionService.releaseHoldByCode(holdCode, username);
        } catch (Exception ignored) {
            // Bỏ qua nếu hold đã được giải phóng hoặc đã hết hạn
        }
    }

    @Test
    @Transactional
    @DisplayName("01. Một người giữ một ghế thành công trong 10 phút")
    void test01_singleUserHoldsSingleSeat() {
        BusTrip trip = busTripService.getAllTrips().get(0);
        Seat seat = findAvailableSeats(trip.getId(), 1).get(0);

        SeatHoldResultDto result = seatSelectionService.holdSeats(trip.getId(), List.of(seat.getId()), "nguyenvanan");

        assertNotNull(result.getHoldCode());
        assertEquals(List.of(seat.getId()), result.getSeatIds());
        assertEquals(List.of(seat.getSeatNumber()), result.getSeatNumbers());
        assertEquals(0, seat.getPrice().compareTo(result.getEstimatedTotal()));

        Seat reloaded = seatRepository.findById(seat.getId()).orElseThrow();
        assertEquals(SeatStatus.HELD, reloaded.getStatus());

        List<SeatHold> activeHolds = seatHoldRepository.findAllBySeatIdAndActiveTrueOrderByIdAsc(seat.getId());
        assertEquals(1, activeHolds.size());
        long mins = Duration.between(activeHolds.get(0).getHeldAt(), activeHolds.get(0).getExpiresAt()).toMinutes();
        assertEquals(10, mins);
    }

    @Test
    @Transactional
    @DisplayName("02. Một người giữ nhiều ghế cùng lúc với tổng tiền chính xác")
    void test02_singleUserHoldsMultipleSeats() {
        BusTrip trip = busTripService.getAllTrips().get(0);
        List<Seat> freeSeats = findAvailableSeats(trip.getId(), 3).subList(0, 3);
        List<Long> ids = freeSeats.stream().map(Seat::getId).toList();
        BigDecimal expectedTotal = freeSeats.stream().map(Seat::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);

        SeatHoldResultDto result = seatSelectionService.holdSeats(trip.getId(), ids, "nguyenvanan");

        assertEquals(3, result.getSeatIds().size());
        assertEquals(0, expectedTotal.compareTo(result.getEstimatedTotal()));
        List<SeatHold> holds = seatHoldRepository.findByHoldCodeAndActiveTrue(result.getHoldCode());
        assertEquals(3, holds.size());
        for (Seat s : freeSeats) {
            assertEquals(SeatStatus.HELD, seatRepository.findById(s.getId()).orElseThrow().getStatus());
        }
    }

    @Test
    @Transactional
    @DisplayName("03. Hai người giữ cùng ghế theo thứ tự - người sau bị chặn tới khi giải phóng")
    void test03_twoUsersHoldSameSeatSequentially() {
        BusTrip trip = busTripService.getAllTrips().get(0);
        Seat target = findAvailableSeats(trip.getId(), 1).get(0);

        SeatHoldResultDto hold1 = seatSelectionService.holdSeats(trip.getId(), List.of(target.getId()), "nguyenvanan");
        assertNotNull(hold1.getHoldCode());

        SeatAlreadyBookedException ex = assertThrows(SeatAlreadyBookedException.class, () ->
                seatSelectionService.holdSeats(trip.getId(), List.of(target.getId()), "tranthibich"));
        assertEquals("SEAT_BEING_HELD", ex.getErrorCode());

        seatSelectionService.releaseHoldByCode(hold1.getHoldCode(), "nguyenvanan");

        SeatHoldResultDto hold2 = seatSelectionService.holdSeats(trip.getId(), List.of(target.getId()), "tranthibich");
        assertNotEquals(hold1.getHoldCode(), hold2.getHoldCode());
        assertEquals(1, seatHoldRepository.findAllBySeatIdAndActiveTrueOrderByIdAsc(target.getId()).size());
    }

    @Test
    @DisplayName("04. Hai người giữ cùng ghế đồng thời đa luồng thực sự - chỉ 1 người thành công, không deadlock")
    void test04_twoUsersHoldSameSeatConcurrently() throws Exception {
        BusTrip trip = busTripService.getAllTrips().get(0);
        List<Seat> freeSeats = findAvailableSeats(trip.getId(), 3);
        Long targetSeatId = freeSeats.get(0).getId();
        Long s2 = freeSeats.get(1).getId();
        Long s3 = freeSeats.get(2).getId();

        List<String> createdHoldCodes = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            // Đợt 1: 2 luồng giữ đồng thời chính xác cùng 1 ghế
            CountDownLatch ready1 = new CountDownLatch(2);
            CountDownLatch start1 = new CountDownLatch(1);
            AtomicInteger successCount1 = new AtomicInteger(0);
            AtomicInteger conflictCount1 = new AtomicInteger(0);
            AtomicInteger unexpectedErrors1 = new AtomicInteger(0);

            String[] users = {"nguyenvanan", "tranthibich"};
            List<Future<?>> futures1 = new ArrayList<>();
            for (String username : users) {
                futures1.add(executor.submit(() -> {
                    ready1.countDown();
                    try {
                        start1.await(5, TimeUnit.SECONDS);
                        SeatHoldResultDto dto = seatSelectionService.holdSeats(trip.getId(), List.of(targetSeatId), username);
                        createdHoldCodes.add(dto.getHoldCode() + "::" + username);
                        successCount1.incrementAndGet();
                    } catch (SeatAlreadyBookedException ex) {
                        conflictCount1.incrementAndGet();
                    } catch (BusinessException ex) {
                        conflictCount1.incrementAndGet();
                    } catch (Throwable t) {
                        unexpectedErrors1.incrementAndGet();
                    }
                }));
            }

            assertTrue(ready1.await(5, TimeUnit.SECONDS));
            start1.countDown();
            for (Future<?> f : futures1) {
                f.get(10, TimeUnit.SECONDS);
            }

            assertEquals(0, unexpectedErrors1.get(), "Không được xảy ra lỗi ngoài dự kiến");
            assertEquals(1, successCount1.get(), "Chỉ đúng 1 người giữ ghế thành công");
            assertEquals(1, conflictCount1.get(), "Người còn lại phải bị từ chối giữ trùng");
            assertEquals(1, seatHoldRepository.findAllBySeatIdAndActiveTrueOrderByIdAsc(targetSeatId).size(),
                    "Chỉ tồn tại duy nhất 1 SeatHold active cho ghế bị tranh chấp");
            assertEquals(SeatStatus.HELD, seatRepository.findById(targetSeatId).orElseThrow().getStatus());

            // Đợt 2: Hai luồng truyền thứ tự ghế ngược nhau [s2, s3] vs [s3, s2] để kiểm chứng chống deadlock
            CountDownLatch ready2 = new CountDownLatch(2);
            CountDownLatch start2 = new CountDownLatch(1);
            AtomicInteger successCount2 = new AtomicInteger(0);
            AtomicInteger conflictCount2 = new AtomicInteger(0);

            List<Future<?>> futures2 = new ArrayList<>();
            futures2.add(executor.submit(() -> {
                ready2.countDown();
                try {
                    start2.await(5, TimeUnit.SECONDS);
                    SeatHoldResultDto dto = seatSelectionService.holdSeats(trip.getId(), List.of(s2, s3), "nguyenvanan");
                    createdHoldCodes.add(dto.getHoldCode() + "::nguyenvanan");
                    successCount2.incrementAndGet();
                } catch (BusinessException ex) {
                    conflictCount2.incrementAndGet();
                } catch (InterruptedException ignored) {
                }
            }));
            futures2.add(executor.submit(() -> {
                ready2.countDown();
                try {
                    start2.await(5, TimeUnit.SECONDS);
                    SeatHoldResultDto dto = seatSelectionService.holdSeats(trip.getId(), List.of(s3, s2), "lehoangnam");
                    createdHoldCodes.add(dto.getHoldCode() + "::lehoangnam");
                    successCount2.incrementAndGet();
                } catch (BusinessException ex) {
                    conflictCount2.incrementAndGet();
                } catch (InterruptedException ignored) {
                }
            }));

            assertTrue(ready2.await(5, TimeUnit.SECONDS));
            start2.countDown();
            for (Future<?> f : futures2) {
                f.get(10, TimeUnit.SECONDS);
            }
            assertEquals(1, successCount2.get());
            assertEquals(1, conflictCount2.get());
        } finally {
            executor.shutdownNow();
            for (String entry : createdHoldCodes) {
                String[] parts = entry.split("::");
                safeReleaseHold(parts[0], parts[1]);
            }
        }
    }

    @Test
    @DisplayName("05. Đổi danh sách ghế giữ (A1,A2 -> A2,A3) và giữ nguyên phiên cũ nếu đổi thất bại")
    void test05_userChangesHeldSeats() {
        BusTrip trip = busTripService.getAllTrips().get(0);
        List<Seat> freeSeats = findAvailableSeats(trip.getId(), 4);
        Long a1 = freeSeats.get(0).getId();
        Long a2 = freeSeats.get(1).getId();
        Long a3 = freeSeats.get(2).getId();
        Long seatOfOtherUser = freeSeats.get(3).getId();

        SeatHoldResultDto hold1 = null;
        SeatHoldResultDto hold2 = null;
        SeatHoldResultDto otherHold = null;

        try {
            hold1 = seatSelectionService.holdSeats(trip.getId(), List.of(a1, a2), "nguyenvanan");
            hold2 = seatSelectionService.changeHeldSeats(hold1.getHoldCode(), List.of(a2, a3), "nguyenvanan");

            assertEquals(SeatStatus.AVAILABLE, seatRepository.findById(a1).orElseThrow().getStatus(), "A1 phải về AVAILABLE");
            assertEquals(SeatStatus.HELD, seatRepository.findById(a2).orElseThrow().getStatus(), "A2 vẫn giữ HELD");
            assertEquals(SeatStatus.HELD, seatRepository.findById(a3).orElseThrow().getStatus(), "A3 chuyển sang HELD");

            assertEquals(0, seatHoldRepository.findAllBySeatIdAndActiveTrueOrderByIdAsc(a1).size());
            assertEquals(1, seatHoldRepository.findAllBySeatIdAndActiveTrueOrderByIdAsc(a2).size());
            assertEquals(1, seatHoldRepository.findAllBySeatIdAndActiveTrueOrderByIdAsc(a3).size());

            // Người khác giữ seatOfOtherUser -> nguyenvanan đổi sang [a2, seatOfOtherUser] bị lỗi nhưng [a2, a3] vẫn bảo toàn
            otherHold = seatSelectionService.holdSeats(trip.getId(), List.of(seatOfOtherUser), "tranthibich");
            String currentHoldCode = hold2.getHoldCode();
            assertThrows(SeatAlreadyBookedException.class, () ->
                    seatSelectionService.changeHeldSeats(currentHoldCode, List.of(a2, seatOfOtherUser), "nguyenvanan"));

            List<SeatHold> preserved = seatHoldRepository.findByHoldCodeAndActiveTrue(currentHoldCode);
            assertEquals(2, preserved.size(), "Phiên giữ cũ [A2, A3] phải được bảo toàn khi đổi ghế thất bại");
            assertEquals(SeatStatus.HELD, seatRepository.findById(a2).orElseThrow().getStatus());
            assertEquals(SeatStatus.HELD, seatRepository.findById(a3).orElseThrow().getStatus());
        } finally {
            safeReleaseHold(hold2 != null ? hold2.getHoldCode() : null, "nguyenvanan");
            safeReleaseHold(hold1 != null ? hold1.getHoldCode() : null, "nguyenvanan");
            safeReleaseHold(otherHold != null ? otherHold.getHoldCode() : null, "tranthibich");
        }
    }

    @Test
    @Transactional
    @DisplayName("06. Người dùng chọn lại một ghế trong phiên giữ cũ (A1,A2 -> A2)")
    void test06_userReselectsPreviousSeat() {
        BusTrip trip = busTripService.getAllTrips().get(0);
        List<Seat> freeSeats = findAvailableSeats(trip.getId(), 2);
        Long a1 = freeSeats.get(0).getId();
        Long a2 = freeSeats.get(1).getId();

        seatSelectionService.holdSeats(trip.getId(), List.of(a1, a2), "nguyenvanan");
        SeatHoldResultDto reselected = seatSelectionService.holdSeats(trip.getId(), List.of(a2), "nguyenvanan");

        assertEquals(1, reselected.getSeatIds().size());
        assertEquals(SeatStatus.AVAILABLE, seatRepository.findById(a1).orElseThrow().getStatus());
        assertEquals(SeatStatus.HELD, seatRepository.findById(a2).orElseThrow().getStatus());
        assertEquals(1, seatHoldRepository.findAllBySeatIdAndActiveTrueOrderByIdAsc(a2).size());
    }

    @Test
    @Transactional
    @DisplayName("07. Từ chối khi cố tình giữ quá 6 ghế")
    void test07_holdMoreThanSixSeatsRejected() {
        BusTrip trip = busTripService.getAllTrips().get(0);
        List<Long> sevenIds = findAvailableSeats(trip.getId(), 7).subList(0, 7).stream().map(Seat::getId).toList();

        BusinessException ex = assertThrows(BusinessException.class, () ->
                seatSelectionService.holdSeats(trip.getId(), sevenIds, "nguyenvanan"));
        assertEquals("MAX_SEAT_LIMIT", ex.getErrorCode());
    }

    @Test
    @Transactional
    @DisplayName("08. Từ chối khi giữ ghế đã BOOKED")
    void test08_holdBookedSeatRejected() {
        BusTrip trip = busTripService.getAllTrips().get(0);
        Seat seat = findAvailableSeats(trip.getId(), 1).get(0);
        seat.setStatus(SeatStatus.BOOKED);
        seatRepository.saveAndFlush(seat);

        SeatAlreadyBookedException ex = assertThrows(SeatAlreadyBookedException.class, () ->
                seatSelectionService.holdSeats(trip.getId(), List.of(seat.getId()), "nguyenvanan"));
        assertEquals("SEAT_ALREADY_BOOKED", ex.getErrorCode());
    }

    @Test
    @Transactional
    @DisplayName("09. Từ chối khi giữ ghế không thuộc chuyến xe đang chọn")
    void test09_holdSeatFromAnotherTripRejected() {
        List<BusTrip> trips = busTripService.getAllTrips();
        assertTrue(trips.size() >= 2, "Cần tối thiểu 2 chuyến xe trong DB kiểm thử");
        BusTrip trip1 = trips.get(0);
        BusTrip trip2 = trips.get(1);
        Seat seatFromTrip2 = findAvailableSeats(trip2.getId(), 1).get(0);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                seatSelectionService.holdSeats(trip1.getId(), List.of(seatFromTrip2.getId()), "nguyenvanan"));
        assertEquals("SEAT_NOT_IN_TRIP", ex.getErrorCode());
    }

    @Test
    @Transactional
    @DisplayName("10. Chủ sở hữu hủy phiên giữ ghế trả ghế về AVAILABLE")
    void test10_cancelSeatHold() {
        BusTrip trip = busTripService.getAllTrips().get(0);
        Seat seat = findAvailableSeats(trip.getId(), 1).get(0);
        SeatHoldResultDto hold = seatSelectionService.holdSeats(trip.getId(), List.of(seat.getId()), "nguyenvanan");

        seatSelectionService.releaseHoldByCode(hold.getHoldCode(), "nguyenvanan");

        assertEquals(SeatStatus.AVAILABLE, seatRepository.findById(seat.getId()).orElseThrow().getStatus());
        assertTrue(seatHoldRepository.findByHoldCodeAndActiveTrue(hold.getHoldCode()).isEmpty());
    }

    @Test
    @Transactional
    @DisplayName("11. Phiên giữ hết hạn tự giải phóng và không trả ghế đã BOOKED về AVAILABLE")
    void test11_expiredHoldReleased() {
        BusTrip trip = busTripService.getAllTrips().get(0);
        List<Seat> seats = findAvailableSeats(trip.getId(), 2);
        Seat expiredHeldSeat = seats.get(0);
        Seat bookedSeat = seats.get(1);

        SeatHoldResultDto hold1 = seatSelectionService.holdSeats(trip.getId(), List.of(expiredHeldSeat.getId()), "nguyenvanan");
        SeatHoldResultDto hold2 = seatSelectionService.holdSeats(trip.getId(), List.of(bookedSeat.getId()), "tranthibich");

        // Giả lập hold1 hết hạn; hold2 có seat đã chuyển BOOKED nhưng giả sử bản ghi quá hạn còn tồn tại
        for (SeatHold h : seatHoldRepository.findByHoldCodeAndActiveTrue(hold1.getHoldCode())) {
            h.setExpiresAt(LocalDateTime.now().minusMinutes(2));
            seatHoldRepository.save(h);
        }
        bookedSeat.setStatus(SeatStatus.BOOKED);
        seatRepository.save(bookedSeat);
        for (SeatHold h : seatHoldRepository.findByHoldCodeAndActiveTrue(hold2.getHoldCode())) {
            h.setExpiresAt(LocalDateTime.now().minusMinutes(2));
            seatHoldRepository.save(h);
        }
        seatHoldRepository.flush();

        int releasedCount = seatSelectionService.releaseExpiredHolds();
        assertTrue(releasedCount >= 2);
        assertEquals(SeatStatus.AVAILABLE, seatRepository.findById(expiredHeldSeat.getId()).orElseThrow().getStatus());
        assertEquals(SeatStatus.BOOKED, seatRepository.findById(bookedSeat.getId()).orElseThrow().getStatus(),
                "Tuyệt đối không trả ghế BOOKED về AVAILABLE");
    }

    @Test
    @Transactional
    @DisplayName("12. Gia hạn giữ ghế hợp lệ cộng thêm thời gian trong giới hạn 15 phút")
    void test12_extendHoldValid() {
        BusTrip trip = busTripService.getAllTrips().get(0);
        Seat seat = findAvailableSeats(trip.getId(), 1).get(0);
        SeatHoldResultDto hold = seatSelectionService.holdSeats(trip.getId(), List.of(seat.getId()), "nguyenvanan");

        long beforeSeconds = seatLockService.getRemainingHoldSeconds(hold.getHoldCode(), "nguyenvanan");
        SeatHoldResultDto extended = seatLockService.extendHold(hold.getHoldCode(), "nguyenvanan", 3);

        assertTrue(extended.getExpiresAt().isAfter(hold.getExpiresAt()));
        assertTrue(extended.getRemainingSeconds() > beforeSeconds + 150);
        assertTrue(extended.isCanExtend());
    }

    @Test
    @Transactional
    @DisplayName("13. Gia hạn vượt trần 15 phút bị khóa ở mức tối đa và từ chối gia hạn thêm")
    void test13_extendHoldExceedingMaxLimit() {
        BusTrip trip = busTripService.getAllTrips().get(0);
        Seat seat = findAvailableSeats(trip.getId(), 1).get(0);
        SeatHoldResultDto hold = seatSelectionService.holdSeats(trip.getId(), List.of(seat.getId()), "nguyenvanan");

        // Lần 1: +3 phút -> 13 phút
        SeatHoldResultDto ext1 = seatLockService.extendHold(hold.getHoldCode(), "nguyenvanan", 3);
        assertTrue(ext1.isCanExtend());

        // Lần 2: +5 phút -> bị trần về đúng heldAt + 15 phút
        SeatHoldResultDto ext2 = seatLockService.extendHold(hold.getHoldCode(), "nguyenvanan", 5);
        assertFalse(ext2.isCanExtend());
        assertEquals(15, Duration.between(ext2.getHeldAt(), ext2.getExpiresAt()).toMinutes());

        // Lần 3: đã đạt trần 15 phút -> phải ném lỗi và KHÔNG cộng thêm thời gian
        BusinessException maxEx = assertThrows(BusinessException.class, () ->
                seatLockService.extendHold(hold.getHoldCode(), "nguyenvanan", 3));
        assertEquals("HOLD_MAX_LIMIT_REACHED", maxEx.getErrorCode());
    }

    @Test
    @Transactional
    @DisplayName("14. Tài khoản khác cố tình hủy phiên giữ ghế phải bị từ chối")
    void test14_otherUserCannotCancelHold() {
        BusTrip trip = busTripService.getAllTrips().get(0);
        Seat seat = findAvailableSeats(trip.getId(), 1).get(0);
        SeatHoldResultDto hold = seatSelectionService.holdSeats(trip.getId(), List.of(seat.getId()), "nguyenvanan");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                seatSelectionService.releaseHoldByCode(hold.getHoldCode(), "tranthibich"));
        assertEquals("UNAUTHORIZED_HOLD", ex.getErrorCode());
        assertEquals(SeatStatus.HELD, seatRepository.findById(seat.getId()).orElseThrow().getStatus());
    }

    @Test
    @Transactional
    @DisplayName("15. Tài khoản khác cố tình gia hạn phiên giữ ghế phải bị từ chối")
    void test15_otherUserCannotExtendHold() {
        BusTrip trip = busTripService.getAllTrips().get(0);
        Seat seat = findAvailableSeats(trip.getId(), 1).get(0);
        SeatHoldResultDto hold = seatSelectionService.holdSeats(trip.getId(), List.of(seat.getId()), "nguyenvanan");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                seatLockService.extendHold(hold.getHoldCode(), "tranthibich", 3));
        assertEquals("UNAUTHORIZED_HOLD", ex.getErrorCode());
    }

    @Test
    @Transactional
    @DisplayName("16. Thanh toán thành công chuyển ghế HELD -> BOOKED và chống tạo vé trùng khi gửi lại request")
    void test16_paymentSuccessMarksSeatsBooked() {
        BusTrip trip = busTripService.getAllTrips().get(0);
        Seat seat = findAvailableSeats(trip.getId(), 1).get(0);
        SeatHoldResultDto hold = seatSelectionService.holdSeats(trip.getId(), List.of(seat.getId()), "nguyenvanan");

        BookingCheckoutDto checkout = new BookingCheckoutDto();
        checkout.setHoldCode(hold.getHoldCode());
        checkout.setPassengerName("Nguyễn Văn An");
        checkout.setPassengerPhone("0912345678");
        checkout.setPassengerEmail("an.nguyen@gmail.com");
        checkout.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        checkout.setSimulatePaymentFailure(false);

        List<Ticket> tickets = ticketBookingService.confirmBookingAndPay(checkout, "nguyenvanan");
        assertEquals(1, tickets.size());
        assertEquals(TicketStatus.CONFIRMED, tickets.get(0).getTicketStatus());
        assertEquals(PaymentStatus.PAID, tickets.get(0).getPaymentStatus());
        assertEquals(SeatStatus.BOOKED, seatRepository.findById(seat.getId()).orElseThrow().getStatus());
        assertTrue(seatHoldRepository.findByHoldCodeAndActiveTrue(hold.getHoldCode()).isEmpty());

        // Không cho phép gia hạn phiên đã chuyển thành vé
        BusinessException extendBookedEx = assertThrows(BusinessException.class, () ->
                seatLockService.extendHold(hold.getHoldCode(), "nguyenvanan", 2));
        assertEquals("HOLD_ALREADY_BOOKED", extendBookedEx.getErrorCode());

        // Không tạo vé trùng nếu gửi lại cùng holdCode
        assertThrows(BusinessException.class, () ->
                ticketBookingService.confirmBookingAndPay(checkout, "nguyenvanan"));
    }

    @Test
    @DisplayName("17. Thanh toán thất bại không đánh dấu BOOKED và vẫn bảo toàn ghế đang HELD")
    void test17_paymentFailureDoesNotBookSeats() {
        BusTrip trip = busTripService.getAllTrips().get(0);
        Seat seat = findAvailableSeats(trip.getId(), 1).get(0);
        SeatHoldResultDto hold = seatSelectionService.holdSeats(trip.getId(), List.of(seat.getId()), "nguyenvanan");

        try {
            BookingCheckoutDto checkout = new BookingCheckoutDto();
            checkout.setHoldCode(hold.getHoldCode());
            checkout.setPassengerName("Nguyễn Văn An");
            checkout.setPassengerPhone("0912345678");
            checkout.setPassengerEmail("an.nguyen@gmail.com");
            checkout.setPaymentMethod(PaymentMethod.E_WALLET);
            checkout.setSimulatePaymentFailure(true);

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    ticketBookingService.confirmBookingAndPay(checkout, "nguyenvanan"));
            assertEquals("PAYMENT_FAILED", ex.getErrorCode());

            assertEquals(SeatStatus.HELD, seatRepository.findById(seat.getId()).orElseThrow().getStatus());
            assertEquals(1, seatHoldRepository.findByHoldCodeAndActiveTrue(hold.getHoldCode()).size());
        } finally {
            safeReleaseHold(hold.getHoldCode(), "nguyenvanan");
        }
    }

    @Test
    @DisplayName("18. Phiên giữ hết hạn không thể xác nhận thanh toán tại Module 5")
    void test18_expiredHoldCannotConfirmPayment() {
        BusTrip trip = busTripService.getAllTrips().get(0);
        Seat seat = findAvailableSeats(trip.getId(), 1).get(0);
        SeatHoldResultDto hold = seatSelectionService.holdSeats(trip.getId(), List.of(seat.getId()), "nguyenvanan");

        try {
            for (SeatHold sh : seatHoldRepository.findByHoldCodeAndActiveTrue(hold.getHoldCode())) {
                sh.setExpiresAt(LocalDateTime.now().minusMinutes(1));
                seatHoldRepository.save(sh);
            }

            BookingCheckoutDto checkout = new BookingCheckoutDto();
            checkout.setHoldCode(hold.getHoldCode());
            checkout.setPassengerName("Nguyễn Văn An");
            checkout.setPassengerPhone("0912345678");
            checkout.setPassengerEmail("an.nguyen@gmail.com");
            checkout.setPaymentMethod(PaymentMethod.BANK_TRANSFER);

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    ticketBookingService.confirmBookingAndPay(checkout, "nguyenvanan"));
            assertEquals("HOLD_EXPIRED", ex.getErrorCode());
            assertEquals(SeatStatus.AVAILABLE, seatRepository.findById(seat.getId()).orElseThrow().getStatus());
        } finally {
            safeReleaseHold(hold.getHoldCode(), "nguyenvanan");
        }
    }

    @Test
    @Transactional
    @DisplayName("19. Gợi ý ghế thông minh ưu tiên ghế liền kề cùng hàng/tầng và xử lý khi không đủ ghế")
    void test19_smartSeatSuggestionPrioritizesAdjacentAndFloor() {
        BusTrip twoFloorTrip = busTripService.getAllTrips().stream()
                .filter(t -> t.getBusType().getFloors() == 2)
                .findFirst()
                .orElse(busTripService.getAllTrips().get(0));

        Map<String, Object> f2Suggest = seatService.suggestSmartSeats(twoFloorTrip.getId(), 2, 2);
        assertEquals(true, f2Suggest.get("sufficient"));
        @SuppressWarnings("unchecked")
        List<SeatStatusDto> f2Seats = (List<SeatStatusDto>) f2Suggest.get("seats");
        assertEquals(2, f2Seats.size());
        assertEquals(2, f2Seats.get(0).getFloorNumber());
        assertEquals(2, f2Seats.get(1).getFloorNumber());
        assertEquals(f2Seats.get(0).getRowIndex(), f2Seats.get(1).getRowIndex());

        // Giới hạn tối đa 6 ghế
        assertThrows(BusinessException.class, () ->
                seatService.suggestSmartSeats(twoFloorTrip.getId(), 7, 1));

        // Đánh dấu phần lớn ghế không khả dụng để kiểm tra thiếu ghế
        List<Seat> available = findAvailableSeats(twoFloorTrip.getId(), 3);
        for (int i = 2; i < available.size(); i++) {
            available.get(i).setStatus(SeatStatus.UNAVAILABLE);
        }
        seatRepository.saveAllAndFlush(available);

        Map<String, Object> insufficient = seatService.suggestSmartSeats(twoFloorTrip.getId(), 4, 1);
        assertEquals(false, insufficient.get("sufficient"));
        @SuppressWarnings("unchecked")
        List<SeatStatusDto> empty = (List<SeatStatusDto>) insufficient.get("seats");
        assertTrue(empty.isEmpty());
    }

    @Test
    @Transactional
    @DisplayName("20. Tính tổng tiền ghế chính xác từ đơn giá gốc DB và chặn ghế không khả dụng")
    void test20_calculateTotalPriceAccuracy() {
        BusTrip trip = busTripService.getAllTrips().get(0);
        List<Seat> freeSeats = findAvailableSeats(trip.getId(), 3);
        Seat s1 = freeSeats.get(0);
        Seat s2 = freeSeats.get(1);
        Seat unavailableSeat = freeSeats.get(2);

        Map<String, Object> summary = seatService.calculateSelectionTotal(trip.getId(), List.of(s1.getId(), s2.getId()));
        BigDecimal expected = s1.getPrice().add(s2.getPrice());
        assertEquals(2, summary.get("seatCount"));
        assertEquals(0, expected.compareTo((BigDecimal) summary.get("totalAmount")));

        unavailableSeat.setStatus(SeatStatus.UNAVAILABLE);
        seatRepository.saveAndFlush(unavailableSeat);

        SeatAlreadyBookedException ex = assertThrows(SeatAlreadyBookedException.class, () ->
                seatService.calculateSelectionTotal(trip.getId(), List.of(unavailableSeat.getId())));
        assertEquals("SEAT_UNAVAILABLE", ex.getErrorCode());
    }
}
