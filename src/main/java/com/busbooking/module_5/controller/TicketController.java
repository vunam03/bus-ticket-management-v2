package com.busbooking.module_5.controller;

import com.busbooking.common.enums.RoleName;
import com.busbooking.common.enums.TicketStatus;
import com.busbooking.common.exception.BusinessException;
import com.busbooking.module_5.dto.TicketDetailDto;
import com.busbooking.module_5.model.Ticket;
import com.busbooking.module_5.service.PaymentService;
import com.busbooking.module_5.service.QrCodeAndPdfService;
import com.busbooking.module_5.service.TicketBookingService;
import com.busbooking.module_5.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Controller mở rộng cho tra cứu vé theo mã, màn hình đặt vé thành công và REST API vé (Module 5).
 */
@Controller
public class TicketController {

    private final TicketService ticketService;
    private final TicketBookingService ticketBookingService;
    private final PaymentService paymentService;
    private final QrCodeAndPdfService qrCodeAndPdfService;

    public TicketController(TicketService ticketService,
                            TicketBookingService ticketBookingService,
                            PaymentService paymentService,
                            QrCodeAndPdfService qrCodeAndPdfService) {
        this.ticketService = ticketService;
        this.ticketBookingService = ticketBookingService;
        this.paymentService = paymentService;
        this.qrCodeAndPdfService = qrCodeAndPdfService;
    }

    /**
     * Tra cứu vé nhanh theo mã vé hoặc lọc theo trạng thái trên trang Vé của tôi.
     */
    @GetMapping("/tickets/lookup")
    public String lookupTicket(@RequestParam(value = "code", required = false) String ticketCode,
                               @RequestParam(value = "status", required = false) TicketStatus status,
                               Authentication auth,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }
        boolean admin = isAdmin(auth);
        if (ticketCode != null && !ticketCode.isBlank()) {
            try {
                Ticket found = ticketService.findTicketByCodeForUser(ticketCode, auth.getName(), admin);
                return "redirect:/tickets/" + found.getId();
            } catch (BusinessException ex) {
                redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
                return "redirect:/tickets/my-tickets";
            }
        }
        List<Ticket> tickets = ticketService.findUserTicketsByStatus(auth.getName(), status);
        model.addAttribute("tickets", tickets);
        model.addAttribute("selectedStatus", status);
        return "module_5/my-tickets";
    }

    /**
     * Hiển thị màn hình chúc mừng đặt vé thành công theo danh sách mã vé.
     */
    @GetMapping("/booking/success")
    public String showBookingSuccess(@RequestParam(value = "codes", required = false) String codesParam,
                                     Authentication auth,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }
        boolean admin = isAdmin(auth);
        if (codesParam == null || codesParam.isBlank()) {
            return "redirect:/tickets/my-tickets";
        }
        List<String> codeList = Arrays.stream(codesParam.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        try {
            List<Ticket> tickets = ticketService.findTicketsByCodesForUser(codeList, auth.getName(), admin);
            if (tickets.isEmpty()) {
                return "redirect:/tickets/my-tickets";
            }
            Ticket primary = tickets.get(0);
            model.addAttribute("tickets", tickets);
            model.addAttribute("primaryTicket", primary);
            model.addAttribute("qrSvg", qrCodeAndPdfService.generateQrSvg(primary.getQrCodePayload()));
            return "module_5/booking-success";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/tickets/my-tickets";
        }
    }

    /**
     * API JSON trả về chi tiết vé điện tử kèm QR SVG và chính sách hoàn hủy.
     */
    @GetMapping("/api/tickets/{id}")
    @ResponseBody
    public ResponseEntity<?> getTicketDetailApi(@PathVariable Long id, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "UNAUTHORIZED", "message", "Vui lòng đăng nhập để xem vé."));
        }
        TicketDetailDto dto = ticketService.buildTicketDetailDto(id, auth.getName(), isAdmin(auth));
        return ResponseEntity.ok(dto);
    }

    /**
     * API JSON tính trước mức hoàn tiền khi hành khách chuẩn bị hủy vé.
     */
    @GetMapping("/api/tickets/{id}/refund-preview")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRefundPreviewApi(@PathVariable Long id, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "UNAUTHORIZED", "message", "Vui lòng đăng nhập để kiểm tra hoàn tiền."));
        }
        boolean admin = isAdmin(auth);
        Ticket ticket = ticketBookingService.getTicketByIdForUser(id, auth.getName(), admin);
        return ResponseEntity.ok(paymentService.calculateRefundPreview(ticket, admin));
    }

    private boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> RoleName.ROLE_ADMIN.name().equals(a.getAuthority()));
    }
}
