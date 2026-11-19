package com.busbooking.module_5;

import com.busbooking.module_1.service.BusTripService;
import com.busbooking.module_4.dto.SeatHoldResultDto;
import com.busbooking.module_4.service.SeatSelectionService;
import com.busbooking.module_5.dto.BookingCheckoutDto;
import com.busbooking.module_5.service.QrCodeAndPdfService;
import com.busbooking.module_5.service.TicketBookingService;
import com.busbooking.common.enums.PaymentMethod;
import com.busbooking.common.enums.PaymentStatus;
import com.busbooking.module_4.enums.SeatStatus;
import com.busbooking.common.enums.TicketStatus;
import com.busbooking.module_1.model.BusTrip;
import com.busbooking.module_4.model.Seat;
import com.busbooking.module_5.model.Ticket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class Module5BookingPaymentTest {

    @Autowired
    private SeatSelectionService seatSelectionService;

    @Autowired
    private TicketBookingService ticketBookingService;

    @Autowired
    private QrCodeAndPdfService qrCodeAndPdfService;

    @Autowired
    private BusTripService busTripService;

    @Test
    @DisplayName("Module 5: Luồng đặt vé hoàn chỉnh, sinh QR SVG, xuất PDF và hủy vé hoàn tiền")
    void shouldCompleteFullBookingQrPdfAndCancelRefundFlow() {
        BusTrip trip = busTripService.getAllTrips().get(0);
        Seat freeSeat = seatSelectionService.getSeatsForTrip(trip.getId()).stream()
                .filter(s -> s.getStatus() == SeatStatus.AVAILABLE)
                .findFirst()
                .orElseThrow();

        SeatHoldResultDto hold = seatSelectionService.holdSeats(
                trip.getId(), List.of(freeSeat.getId()), "nguyenvanan");

        BookingCheckoutDto checkout = new BookingCheckoutDto();
        checkout.setHoldCode(hold.getHoldCode());
        checkout.setPassengerName("Nguyễn Văn An");
        checkout.setPassengerPhone("0912345678");
        checkout.setPassengerEmail("an.nguyen@gmail.com");
        checkout.setPaymentMethod(PaymentMethod.E_WALLET);
        checkout.setSimulateFailure(false);

        List<Ticket> tickets = ticketBookingService.confirmBookingAndPay(checkout, "nguyenvanan");
        assertEquals(1, tickets.size());
        Ticket booked = tickets.get(0);

        assertEquals(TicketStatus.CONFIRMED, booked.getStatus());
        assertEquals(PaymentStatus.PAID, booked.getPaymentStatus());

        // Kiểm tra sinh QR SVG và file PDF hợp lệ
        String svg = qrCodeAndPdfService.generateQrSvg(booked.getQrCodePayload());
        assertTrue(svg.contains("<svg"));

        byte[] pdfBytes = qrCodeAndPdfService.generateTicketPdf(booked);
        String pdfHeader = new String(pdfBytes, 0, 8, StandardCharsets.US_ASCII);
        assertTrue(pdfHeader.startsWith("%PDF-1.4"));

        // Kiểm tra hủy vé và hoàn tiền
        Ticket cancelled = ticketBookingService.cancelTicket(
                booked.getId(), "Đổi lịch công tác", "nguyenvanan", false);
        assertEquals(TicketStatus.CANCELLED, cancelled.getStatus());
        assertEquals(PaymentStatus.REFUNDED, cancelled.getPaymentStatus());
        assertNotNull(cancelled.getRefundAmount());
        assertTrue(cancelled.getRefundAmount().doubleValue() > 0);
    }
}
