package com.busbooking.module_5.controller;

import com.busbooking.module_5.service.TicketBookingService;
import com.busbooking.common.exception.BusinessException;
import com.busbooking.module_5.model.Ticket;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminTicketPaymentController {

    private final TicketBookingService bookingService;

    public AdminTicketPaymentController(TicketBookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/tickets")
    public String listAllTickets(Model model) {
        model.addAttribute("tickets", bookingService.getAllTicketsForAdmin());
        model.addAttribute("activeMenu", "tickets");
        return "module_5/admin-tickets";
    }

    @GetMapping("/payments")
    public String listAllPayments(Model model) {
        model.addAttribute("transactions", bookingService.getAllTransactionsForAdmin());
        model.addAttribute("activeMenu", "payments");
        return "module_5/admin-payments";
    }

    @PostMapping("/tickets/{id}/confirm-payment")
    public String confirmCashPayment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Ticket ticket = bookingService.confirmCashPaymentByAdmin(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã xác nhận thu tiền thành công cho vé " + ticket.getTicketCode() + "!");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/tickets";
    }

    @PostMapping("/tickets/{id}/cancel")
    public String cancelTicketByAdmin(@PathVariable Long id,
                                      @RequestParam(value = "reason", defaultValue = "Admin hủy vé theo yêu cầu") String reason,
                                      Authentication auth,
                                      RedirectAttributes redirectAttributes) {
        try {
            Ticket cancelled = bookingService.cancelTicket(id, reason, auth.getName(), true);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Admin đã hủy vé " + cancelled.getTicketCode() + " và giải phóng ghế " + cancelled.getSeat().getSeatNumber() + "!");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/tickets";
    }
}
