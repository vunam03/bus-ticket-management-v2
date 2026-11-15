package com.busbooking.module_4;

import com.busbooking.module_1.service.BusTripService;
import com.busbooking.module_4.dto.SeatHoldResultDto;
import com.busbooking.module_4.service.SeatSelectionService;
import com.busbooking.shared.enums.SeatStatus;
import com.busbooking.shared.exception.BusinessException;
import com.busbooking.shared.model.BusTrip;
import com.busbooking.shared.model.Seat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class Module4SeatHoldTest {

    @Autowired
    private SeatSelectionService seatSelectionService;

    @Autowired
    private BusTripService busTripService;

    @Test
    @DisplayName("Module 4: Giữ ghế tạm thời 10 phút và chống 2 người đặt trùng cùng 1 ghế")
    void shouldHoldSeatsAndPreventDuplicateSelection() {
        BusTrip trip = busTripService.getAllTrips().get(0);
        List<Seat> availableSeats = seatSelectionService.getSeatsForTrip(trip.getId()).stream()
                .filter(s -> s.getStatus() == SeatStatus.AVAILABLE)
                .toList();
        assertTrue(availableSeats.size() >= 2);

        Long targetSeatId = availableSeats.get(0).getId();

        SeatHoldResultDto holdResult = seatSelectionService.holdSeats(
                trip.getId(), List.of(targetSeatId), "nguyenvanan");

        assertNotNull(holdResult.getHoldCode());
        assertEquals(1, holdResult.getSeatNumbers().size());

        // Người thứ 2 cố tình chọn trùng ghế đang giữ sẽ bị chặn
        assertThrows(BusinessException.class, () ->
                seatSelectionService.holdSeats(trip.getId(), List.of(targetSeatId), "tranthibich"));

        // Giải phóng ghế giữ tạm
        seatSelectionService.releaseHoldByCode(holdResult.getHoldCode(), "nguyenvanan");
        Seat releasedSeat = seatSelectionService.getSeatsForTrip(trip.getId()).stream()
                .filter(s -> s.getId().equals(targetSeatId))
                .findFirst()
                .orElseThrow();
        assertEquals(SeatStatus.AVAILABLE, releasedSeat.getStatus());
    }
}
