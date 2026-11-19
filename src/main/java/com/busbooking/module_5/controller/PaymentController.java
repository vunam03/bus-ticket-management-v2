package com.busbooking.module_5.controller;

import com.busbooking.common.enums.RoleName;
import com.busbooking.common.exception.BusinessException;
import com.busbooking.module_5.dto.BookingRequest;
import com.busbooking.module_5.dto.CheckoutResponse;
import com.busbooking.module_5.dto.PaymentGatewayDto;
import com.busbooking.module_5.model.Ticket;
import com.busbooking.module_5.service.BookingService;
import com.busbooking.module_5.service.PaymentService;
import com.busbooking.module_5.service.QrCodeAndPdfService;
import com.busbooking.module_5.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * Controller cung cấp API phương thức thanh toán, mô phỏng đối soát giao dịch và trang kết quả thanh toán (Module 5).
 */
@Controller
public class PaymentController {

    private final PaymentService paymentService;
    private final BookingService bookingService;
    private final TicketService ticketService;
    private final QrCodeAndPdfService qrCodeAndPdfService;

    public PaymentController(PaymentService paymentService,
                             BookingService bookingService,
                             TicketService ticketService,
                             QrCodeAndPdfService qrCodeAndPdfService) {
        this.paymentService = paymentService;
        this.bookingService = bookingService;
        this.ticketService = ticketService;
        this.qrCodeAndPdfService = qrCodeAndPdfService;
    }

    /**
     * API JSON liệt kê các cổng thanh toán hỗ trợ trên hệ thống.
     */
    @GetMapping("/api/payments/methods")
    @ResponseBody
    public ResponseEntity<List<PaymentGatewayDto>> listPaymentMethodsApi() {
        return ResponseEntity.ok(paymentService.getSupportedGateways());
    }

    /**
     * API JSON thực thi xác nhận thanh toán đặt vé từ phiên giữ chỗ 10 phút.
     */
    @PostMapping("/api/payments/simulate")
    @ResponseBody
    public ResponseEntity<?> simulatePaymentApi(@Valid @RequestBody BookingRequest request, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "UNAUTHORIZED", "message", "Vui lòng đăng nhập để thanh toán."));
        }
        try {
            CheckoutResponse response = bookingService.processBooking(request, auth.getName());
            return ResponseEntity.ok(response);
        } catch (BusinessException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", ex.getErrorCode(),
                    "message", ex.getMessage()
            ));
        }
    }

    /**
     * Hiển thị màn hình kết quả thanh toán thành công cho một mã vé cụ thể (/booking/payment-result).
     */
    @GetMapping("/booking/payment-result")
    public String paymentResultPage(@RequestParam("ticketCode") String ticketCode,
                                    Authentication auth,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }
        boolean admin = auth.getAuthorities().stream()
                .anyMatch(a -> RoleName.ROLE_ADMIN.name().equals(a.getAuthority()));
        try {
            Ticket ticket = ticketService.findTicketByCodeForUser(ticketCode, auth.getName(), admin);
            model.addAttribute("tickets", List.of(ticket));
            model.addAttribute("primaryTicket", ticket);
            model.addAttribute("qrSvg", qrCodeAndPdfService.generateQrSvg(ticket.getQrCodePayload()));
            return "module_5/booking-success";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/tickets/my-tickets";
        }
    }
}
