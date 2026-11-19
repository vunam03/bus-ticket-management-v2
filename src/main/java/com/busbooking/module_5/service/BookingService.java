package com.busbooking.module_5.service;

import com.busbooking.common.utils.CurrencyUtils;
import com.busbooking.common.utils.DateTimeUtils;
import com.busbooking.module_1.model.BusTrip;
import com.busbooking.module_4.model.SeatHold;
import com.busbooking.module_4.service.SeatSelectionService;
import com.busbooking.module_5.dto.BookingRequest;
import com.busbooking.module_5.dto.CheckoutResponse;
import com.busbooking.module_5.model.Ticket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dịch vụ điều phối phiên Checkout và đóng gói phản hồi kết quả đặt vé (Module 5).
 */
@Service
public class BookingService {

    private final TicketBookingService ticketBookingService;
    private final SeatSelectionService seatSelectionService;

    public BookingService(TicketBookingService ticketBookingService,
                          SeatSelectionService seatSelectionService) {
        this.ticketBookingService = ticketBookingService;
        this.seatSelectionService = seatSelectionService;
    }

    /**
     * Tổng hợp thông tin phiên giữ ghế trước khi khách hàng nhấn thanh toán.
     */
    @Transactional
    public CheckoutResponse buildCheckoutSummary(String holdCode, String username) {
        List<SeatHold> holds = seatSelectionService.getValidHoldsByCode(holdCode, username);
        BusTrip trip = holds.get(0).getSeat().getBusTrip();
        BigDecimal totalAmount = holds.stream()
                .map(hold -> hold.getSeat().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<String> seatNumbers = holds.stream()
                .map(hold -> hold.getSeat().getSeatNumber())
                .collect(Collectors.toList());

        CheckoutResponse response = new CheckoutResponse();
        response.setHoldCode(holdCode);
        response.setTripCode(trip.getTripCode());
        response.setRouteName(trip.getDepartureProvince() + " → " + trip.getArrivalProvince());
        response.setDepartureTimeText(DateTimeUtils.formatDateTime(trip.getDepartureTime()));
        response.setSeatNumbers(seatNumbers);
        response.setTotalAmount(totalAmount);
        response.setFormattedTotal(CurrencyUtils.formatVnd(totalAmount));
        response.setHoldExpiresAt(holds.get(0).getExpiresAt());
        response.setPaymentSuccess(false);
        response.setMessage("Ghế đang được giữ tạm thời trong 10 phút.");
        return response;
    }

    /**
     * Thực thi đặt vé và thanh toán từ BookingRequest, trả về CheckoutResponse chứa danh sách mã vé.
     */
    @Transactional
    public CheckoutResponse processBooking(BookingRequest request, String username) {
        List<Ticket> bookedTickets = ticketBookingService.confirmBookingAndPay(request.toCheckoutDto(), username);
        Ticket first = bookedTickets.get(0);
        BusTrip trip = first.getBusTrip();

        BigDecimal grandTotal = bookedTickets.stream()
                .map(Ticket::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CheckoutResponse response = new CheckoutResponse();
        response.setHoldCode(request.getHoldCode());
        response.setTripCode(trip.getTripCode());
        response.setRouteName(trip.getDepartureProvince() + " → " + trip.getArrivalProvince());
        response.setDepartureTimeText(DateTimeUtils.formatDateTime(trip.getDepartureTime()));
        response.setSeatNumbers(bookedTickets.stream().map(t -> t.getSeat().getSeatNumber()).collect(Collectors.toList()));
        response.setTotalAmount(grandTotal);
        response.setFormattedTotal(CurrencyUtils.formatVnd(grandTotal));
        response.setPaymentSuccess(true);
        response.setPaymentMethodName(first.getPaymentMethod().getDisplayName());
        response.setTicketIds(bookedTickets.stream().map(Ticket::getId).collect(Collectors.toList()));
        response.setTicketCodes(bookedTickets.stream().map(Ticket::getTicketCode).collect(Collectors.toList()));
        response.setMessage("Đặt vé và ghi nhận thanh toán thành công " + bookedTickets.size() + " vé!");
        return response;
    }
}
