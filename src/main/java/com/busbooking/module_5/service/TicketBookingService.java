package com.busbooking.module_5.service;

import com.busbooking.module_1.service.BusTripService;
import com.busbooking.module_2.service.AuthAccountService;
import com.busbooking.module_4.enums.SeatStatus;
import com.busbooking.module_4.repository.SeatHoldRepository;
import com.busbooking.module_4.repository.SeatSelectionRepository;
import com.busbooking.module_4.service.SeatSelectionService;
import com.busbooking.module_5.dto.BookingCheckoutDto;
import com.busbooking.module_5.repository.BookingTicketRepository;
import com.busbooking.module_5.repository.PaymentTransactionRepository;
import com.busbooking.common.enums.*;
import com.busbooking.common.exception.BusinessException;
import com.busbooking.common.exception.ResourceNotFoundException;
import com.busbooking.module_1.model.BusTrip;
import com.busbooking.module_2.model.User;
import com.busbooking.module_4.model.Seat;
import com.busbooking.module_4.model.SeatHold;
import com.busbooking.module_5.model.PaymentTransaction;
import com.busbooking.module_5.model.Ticket;
import com.busbooking.common.utils.CodeGeneratorUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TicketBookingService {

    private final BookingTicketRepository ticketRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final SeatSelectionService seatSelectionService;
    private final SeatHoldRepository seatHoldRepository;
    private final SeatSelectionRepository seatRepository;
    private final BusTripService busTripService;
    private final AuthAccountService authAccountService;

    public TicketBookingService(BookingTicketRepository ticketRepository,
                                PaymentTransactionRepository transactionRepository,
                                SeatSelectionService seatSelectionService,
                                SeatHoldRepository seatHoldRepository,
                                SeatSelectionRepository seatRepository,
                                BusTripService busTripService,
                                AuthAccountService authAccountService) {
        this.ticketRepository = ticketRepository;
        this.transactionRepository = transactionRepository;
        this.seatSelectionService = seatSelectionService;
        this.seatHoldRepository = seatHoldRepository;
        this.seatRepository = seatRepository;
        this.busTripService = busTripService;
        this.authAccountService = authAccountService;
    }

    /**
     * Xác nhận đặt vé và xử lý thanh toán mô phỏng từ phiên giữ ghế 10 phút.
     */
    @Transactional
    public List<Ticket> confirmBookingAndPay(BookingCheckoutDto dto, String username) {
        List<SeatHold> holds = seatSelectionService.getValidHoldsByCode(dto.getHoldCode(), username);
        User user = authAccountService.getByUsername(username);
        Long tripId = holds.get(0).getSeat().getBusTrip().getId();
        BusTrip trip = busTripService.getTripById(tripId);
        if (trip.getStatus() != TripStatus.SCHEDULED) {
            throw new BusinessException("TRIP_NOT_AVAILABLE", "Chuyến xe này không còn mở bán vé.");
        }

        // Khóa ghế theo thứ tự ID tăng dần ngay thời điểm xác nhận thanh toán để chống tạo vé trùng khi gửi đồng thời
        List<Long> sortedSeatIds = holds.stream()
                .map(h -> h.getSeat().getId())
                .distinct()
                .sorted()
                .toList();
        List<Seat> lockedSeats = seatRepository.findSeatsForHold(tripId, sortedSeatIds);
        if (lockedSeats.size() != sortedSeatIds.size()) {
            throw new BusinessException("INVALID_SEATS", "Không tìm thấy thông tin ghế hợp lệ cho chuyến xe này.");
        }
        Map<Long, Seat> lockedSeatMap = lockedSeats.stream()
                .collect(Collectors.toMap(Seat::getId, Function.identity()));

        List<SeatHold> activeHoldsAfterLock = seatHoldRepository.findByHoldCodeAndActiveTrue(dto.getHoldCode().trim());
        if (activeHoldsAfterLock.size() != sortedSeatIds.size()) {
            throw new BusinessException("SEAT_ALREADY_BOOKED",
                    "Phiên giữ ghế đã được xác nhận thành vé hoặc không còn hiệu lực.");
        }
        for (SeatHold hold : activeHoldsAfterLock) {
            if (!hold.isOwnedBy(user)) {
                throw new BusinessException("UNAUTHORIZED_HOLD", "Bạn không có quyền thanh toán cho phiên giữ ghế của tài khoản khác.");
            }
            if (hold.isExpired()) {
                throw new BusinessException("HOLD_EXPIRED", "Phiên giữ ghế đã hết hạn trước khi hoàn tất thanh toán.");
            }
            Seat lockedSeat = lockedSeatMap.get(hold.getSeat().getId());
            if (lockedSeat == null || lockedSeat.getStatus() == SeatStatus.BOOKED) {
                throw new BusinessException("SEAT_ALREADY_BOOKED",
                        "Ghế " + (lockedSeat != null ? lockedSeat.getSeatNumber() : "") + " đã được đặt trước đó.");
            }
            if (lockedSeat.getStatus() != SeatStatus.HELD) {
                throw new BusinessException("SEAT_STATE_INVALID",
                        "Ghế " + lockedSeat.getSeatNumber() + " không còn ở trạng thái giữ chỗ hợp lệ.");
            }
        }

        // Kiểm tra nếu mô phỏng lỗi cổng thanh toán -> giữ nguyên ghế HELD để hành khách thử lại trước khi hết hạn
        if (dto.isSimulatePaymentFailure()) {
            for (SeatHold hold : activeHoldsAfterLock) {
                Seat seat = lockedSeatMap.get(hold.getSeat().getId());
                Ticket failedTicket = new Ticket();
                failedTicket.setTicketCode(CodeGeneratorUtils.generateTicketCode());
                failedTicket.setUser(user);
                failedTicket.setBusTrip(seat.getBusTrip());
                failedTicket.setSeat(seat);
                failedTicket.setPassengerName(dto.getPassengerName().trim());
                failedTicket.setPassengerPhone(dto.getPassengerPhone().trim());
                failedTicket.setPassengerEmail(dto.getPassengerEmail());
                failedTicket.setPickupNote(dto.getPickupNote());
                failedTicket.setTotalAmount(seat.getPrice());
                failedTicket.setPaymentMethod(dto.getPaymentMethod());
                failedTicket.setTicketStatus(TicketStatus.CANCELLED);
                failedTicket.setPaymentStatus(PaymentStatus.FAILED);
                failedTicket.setCancellationReason("Giao dịch thanh toán thất bại");
                failedTicket.setQrCodePayload("INVALID-" + failedTicket.getTicketCode());
                Ticket savedFailed = ticketRepository.save(failedTicket);

                PaymentTransaction failedTxn = new PaymentTransaction(
                        CodeGeneratorUtils.generateTransactionCode(),
                        savedFailed,
                        seat.getPrice(),
                        dto.getPaymentMethod(),
                        TransactionStatus.FAILED,
                        "Thanh toán thất bại qua " + dto.getPaymentMethod().getDisplayName()
                );
                transactionRepository.save(failedTxn);
            }
            throw new BusinessException("PAYMENT_FAILED",
                    "Giao dịch thanh toán không thành công! Ghế của bạn vẫn được giữ trong thời gian còn lại để thử lại.");
        }

        List<Ticket> bookedTickets = new ArrayList<>();

        for (SeatHold hold : activeHoldsAfterLock) {
            Seat seat = lockedSeatMap.get(hold.getSeat().getId());
            if (!seat.getStatus().canTransitionTo(SeatStatus.BOOKED)) {
                throw new BusinessException("SEAT_ALREADY_BOOKED",
                        "Ghế " + seat.getSeatNumber() + " đã được đặt. Không thể hoàn tất giao dịch.");
            }

            // Chỉ tính tiền theo đơn giá gốc của ghế trong DB (không dùng giá từ frontend)
            BigDecimal serverSeatPrice = seat.getPrice();
            seat.setStatus(SeatStatus.BOOKED);
            seatRepository.save(seat);

            hold.setActive(false);
            seatHoldRepository.save(hold);

            String ticketCode = CodeGeneratorUtils.generateTicketCode();
            Ticket ticket = new Ticket();
            ticket.setTicketCode(ticketCode);
            ticket.setUser(user);
            ticket.setBusTrip(seat.getBusTrip());
            ticket.setSeat(seat);
            ticket.setPassengerName(dto.getPassengerName().trim());
            ticket.setPassengerPhone(dto.getPassengerPhone().trim());
            ticket.setPassengerEmail(dto.getPassengerEmail() != null ? dto.getPassengerEmail().trim() : user.getEmail());
            ticket.setPickupNote(dto.getPickupNote() != null ? dto.getPickupNote().trim() : "");
            ticket.setTotalAmount(serverSeatPrice);
            ticket.setPaymentMethod(dto.getPaymentMethod());

            // Nếu thanh toán chuyển khoản hoặc ví điện tử -> xác nhận & thanh toán ngay
            // Nếu tiền mặt -> vé xác nhận chỗ và thu tiền mặt hoặc đánh dấu đã xác nhận
            if (dto.getPaymentMethod() == PaymentMethod.CASH) {
                ticket.setTicketStatus(TicketStatus.CONFIRMED);
                ticket.setPaymentStatus(PaymentStatus.UNPAID);
            } else {
                ticket.setTicketStatus(TicketStatus.CONFIRMED);
                ticket.setPaymentStatus(PaymentStatus.PAID);
            }

            ticket.setQrCodePayload("BUSBOOKING|" + ticketCode + "|" + seat.getBusTrip().getTripCode() + "|" + seat.getSeatNumber());
            Ticket savedTicket = ticketRepository.save(ticket);

            TransactionStatus txnStatus = (dto.getPaymentMethod() == PaymentMethod.CASH)
                    ? TransactionStatus.PENDING
                    : TransactionStatus.SUCCESS;

            PaymentTransaction txn = new PaymentTransaction(
                    CodeGeneratorUtils.generateTransactionCode(),
                    savedTicket,
                    serverSeatPrice,
                    dto.getPaymentMethod(),
                    txnStatus,
                    "Thanh toán vé " + ticketCode + " qua " + dto.getPaymentMethod().getDisplayName()
            );
            transactionRepository.save(txn);
            bookedTickets.add(savedTicket);
        }

        seatRepository.flush();
        seatHoldRepository.flush();
        busTripService.refreshAvailableSeats(tripId);
        return bookedTickets;
    }

    @Transactional(readOnly = true)
    public List<Ticket> getTicketsForUser(String username) {
        User user = authAccountService.getByUsername(username);
        return ticketRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional(readOnly = true)
    public Ticket getTicketByIdForUser(Long ticketId, String username, boolean isAdmin) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé với mã ID: " + ticketId));
        if (!isAdmin) {
            User user = authAccountService.getByUsername(username);
            if (!ticket.getUser().getId().equals(user.getId())) {
                throw new BusinessException("ACCESS_DENIED_TICKET", "Bạn không có quyền xem vé của hành khách khác.");
            }
        }
        return ticket;
    }

    /**
     * Hủy vé và tính tiền hoàn theo chính sách:
     * - Trước giờ khởi hành >= 24 giờ: Hoàn 90% giá vé
     * - Trước giờ khởi hành từ 4 đến < 24 giờ: Hoàn 70% giá vé
     * - Dưới 4 giờ trước giờ khởi hành (khi khách tự hủy): Không đủ điều kiện hủy online
     */
    @Transactional
    public Ticket cancelTicket(Long ticketId, String reason, String username, boolean isAdmin) {
        Ticket ticket = getTicketByIdForUser(ticketId, username, isAdmin);
        if (ticket.getTicketStatus() == TicketStatus.CANCELLED) {
            throw new BusinessException("ALREADY_CANCELLED", "Vé này đã được hủy trước đó.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime departure = ticket.getBusTrip().getDepartureTime();
        long hoursBeforeDeparture = Duration.between(now, departure).toHours();

        if (!isAdmin && hoursBeforeDeparture < 4) {
            throw new BusinessException("CANCEL_DEADLINE_PASSED",
                    "Chỉ có thể hủy vé trực tuyến trước giờ khởi hành tối thiểu 4 tiếng.");
        }

        BigDecimal refundAmount = BigDecimal.ZERO;
        if (ticket.getPaymentStatus() == PaymentStatus.PAID) {
            BigDecimal refundRate = (hoursBeforeDeparture >= 24 || isAdmin)
                    ? new BigDecimal("0.90")
                    : new BigDecimal("0.70");
            refundAmount = ticket.getTotalAmount().multiply(refundRate).setScale(2, RoundingMode.HALF_UP);
            ticket.setPaymentStatus(PaymentStatus.REFUNDED);

            PaymentTransaction refundTxn = new PaymentTransaction(
                    CodeGeneratorUtils.generateTransactionCode(),
                    ticket,
                    refundAmount,
                    ticket.getPaymentMethod(),
                    TransactionStatus.REFUNDED,
                    "Hoàn tiền hủy vé " + ticket.getTicketCode() + " (" + (refundRate.multiply(new BigDecimal("100")).intValue()) + "%)"
            );
            transactionRepository.save(refundTxn);
        }

        ticket.setTicketStatus(TicketStatus.CANCELLED);
        ticket.setRefundAmount(refundAmount);
        ticket.setCancellationReason(reason != null && !reason.isBlank() ? reason.trim() : "Khách hàng yêu cầu hủy vé");
        ticket.setCancelledAt(now);

        Seat seat = seatRepository.findSeatsByIdsWithLock(List.of(ticket.getSeat().getId()))
                .stream()
                .findFirst()
                .orElse(ticket.getSeat());
        if (seat.getStatus() == SeatStatus.BOOKED
                && !seatHoldRepository.existsActiveNonExpiredHoldForSeat(seat.getId(), now)) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seatRepository.save(seat);
        }

        Ticket saved = ticketRepository.save(ticket);
        busTripService.refreshAvailableSeats(ticket.getBusTrip().getId());
        return saved;
    }

    /**
     * Admin xác nhận đã thu tiền mặt đối với vé thanh toán tại quầy/khi lên xe.
     */
    @Transactional
    public Ticket confirmCashPaymentByAdmin(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé ID: " + ticketId));
        if (ticket.getTicketStatus() == TicketStatus.CANCELLED) {
            throw new BusinessException("TICKET_CANCELLED", "Không thể xác nhận thanh toán cho vé đã hủy.");
        }
        ticket.setTicketStatus(TicketStatus.CONFIRMED);
        ticket.setPaymentStatus(PaymentStatus.PAID);
        Ticket saved = ticketRepository.save(ticket);

        List<PaymentTransaction> txns = transactionRepository.findByTicketIdOrderByCreatedAtDesc(ticketId);
        if (!txns.isEmpty()) {
            PaymentTransaction latest = txns.get(0);
            latest.setStatus(TransactionStatus.SUCCESS);
            latest.setDescription(latest.getDescription() + " (Admin đã xác nhận thu tiền)");
            transactionRepository.save(latest);
        } else {
            PaymentTransaction newTxn = new PaymentTransaction(
                    CodeGeneratorUtils.generateTransactionCode(),
                    saved,
                    saved.getTotalAmount(),
                    saved.getPaymentMethod(),
                    TransactionStatus.SUCCESS,
                    "Admin xác nhận thu tiền vé " + saved.getTicketCode()
            );
            transactionRepository.save(newTxn);
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Ticket> getAllTicketsForAdmin() {
        return ticketRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<PaymentTransaction> getAllTransactionsForAdmin() {
        return transactionRepository.findAllByOrderByCreatedAtDesc();
    }
}
