package com.busbooking.module_5.controller;

import com.busbooking.module_2.service.AuthAccountService;
import com.busbooking.module_4.service.SeatSelectionService;
import com.busbooking.module_5.dto.BookingCheckoutDto;
import com.busbooking.module_5.service.QrCodeAndPdfService;
import com.busbooking.module_5.service.TicketBookingService;
import com.busbooking.common.enums.PaymentMethod;
import com.busbooking.common.enums.RoleName;
import com.busbooking.common.exception.BusinessException;
import com.busbooking.module_4.model.SeatHold;
import com.busbooking.module_5.model.Ticket;
import com.busbooking.module_2.model.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class TicketBookingController {

    private final TicketBookingService bookingService;
    private final SeatSelectionService seatSelectionService;
    private final AuthAccountService authAccountService;
    private final QrCodeAndPdfService qrCodeAndPdfService;

    public TicketBookingController(TicketBookingService bookingService,
                                   SeatSelectionService seatSelectionService,
                                   AuthAccountService authAccountService,
                                   QrCodeAndPdfService qrCodeAndPdfService) {
        this.bookingService = bookingService;
        this.seatSelectionService = seatSelectionService;
        this.authAccountService = authAccountService;
        this.qrCodeAndPdfService = qrCodeAndPdfService;
    }

    @GetMapping("/booking/checkout")
    public String showCheckoutPage(@RequestParam("holdCode") String holdCode,
                                   Authentication auth,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        try {
            List<SeatHold> holds = seatSelectionService.getValidHoldsByCode(holdCode, auth.getName());
            User user = authAccountService.getByUsername(auth.getName());

            BookingCheckoutDto checkoutForm = new BookingCheckoutDto();
            checkoutForm.setHoldCode(holdCode);
            checkoutForm.setPassengerName(user.getFullName());
            checkoutForm.setPassengerPhone(user.getPhone() != null ? user.getPhone() : "");
            checkoutForm.setPassengerEmail(user.getEmail());
            checkoutForm.setPaymentMethod(PaymentMethod.BANK_TRANSFER);

            populateCheckoutModel(model, holds, checkoutForm);
            return "module_5/checkout";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/trips/search";
        }
    }

    @PostMapping("/booking/confirm")
    public String confirmBooking(@Valid @ModelAttribute("checkoutForm") BookingCheckoutDto checkoutForm,
                                 BindingResult bindingResult,
                                 Authentication auth,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            List<SeatHold> holds = seatSelectionService.getValidHoldsByCode(checkoutForm.getHoldCode(), auth.getName());
            if (bindingResult.hasErrors()) {
                populateCheckoutModel(model, holds, checkoutForm);
                return "module_5/checkout";
            }

            List<Ticket> createdTickets = bookingService.confirmBookingAndPay(checkoutForm, auth.getName());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đặt vé thành công! Mã vé của bạn: " +
                            createdTickets.stream().map(Ticket::getTicketCode).collect(Collectors.joining(", ")));

            if (createdTickets.size() == 1) {
                return "redirect:/tickets/" + createdTickets.get(0).getId();
            }
            return "redirect:/tickets/my-tickets";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/booking/checkout?holdCode=" + checkoutForm.getHoldCode();
        }
    }

    @GetMapping("/tickets/my-tickets")
    public String showMyTickets(Authentication auth, Model model) {
        List<Ticket> tickets = bookingService.getTicketsForUser(auth.getName());
        model.addAttribute("tickets", tickets);
        return "module_5/my-tickets";
    }

    @GetMapping("/tickets/{id}")
    public String showTicketDetail(@PathVariable Long id, Authentication auth, Model model) {
        boolean isAdmin = isAdmin(auth);
        Ticket ticket = bookingService.getTicketByIdForUser(id, auth.getName(), isAdmin);
        model.addAttribute("ticket", ticket);
        model.addAttribute("qrSvg", qrCodeAndPdfService.generateQrSvg(ticket.getQrCodePayload()));
        return "module_5/ticket-detail";
    }

    @GetMapping("/tickets/{id}/print")
    public String printTicketView(@PathVariable Long id, Authentication auth, Model model) {
        boolean isAdmin = isAdmin(auth);
        Ticket ticket = bookingService.getTicketByIdForUser(id, auth.getName(), isAdmin);
        model.addAttribute("ticket", ticket);
        model.addAttribute("qrSvg", qrCodeAndPdfService.generateQrSvg(ticket.getQrCodePayload()));
        return "module_5/ticket-print";
    }

    @GetMapping("/tickets/{id}/pdf")
    public ResponseEntity<byte[]> downloadTicketPdf(@PathVariable Long id, Authentication auth) {
        boolean isAdmin = isAdmin(auth);
        Ticket ticket = bookingService.getTicketByIdForUser(id, auth.getName(), isAdmin);
        byte[] pdfBytes = qrCodeAndPdfService.generateTicketPdf(ticket);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=VeXe_" + ticket.getTicketCode() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping("/tickets/{id}/cancel")
    public String cancelTicket(@PathVariable Long id,
                               @RequestParam(value = "reason", required = false) String reason,
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {
        try {
            boolean isAdmin = isAdmin(auth);
            Ticket cancelled = bookingService.cancelTicket(id, reason, auth.getName(), isAdmin);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã hủy vé " + cancelled.getTicketCode() + " thành công!");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/tickets/" + id;
    }

    private void populateCheckoutModel(Model model, List<SeatHold> holds, BookingCheckoutDto checkoutForm) {
        BigDecimal totalAmount = holds.stream()
                .map(sh -> sh.getSeat().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<String> seatNumbers = holds.stream()
                .map(sh -> sh.getSeat().getSeatNumber())
                .collect(Collectors.toList());

        model.addAttribute("checkoutForm", checkoutForm);
        model.addAttribute("holds", holds);
        model.addAttribute("trip", holds.get(0).getSeat().getBusTrip());
        model.addAttribute("seatNumbers", seatNumbers);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("expiresAt", holds.get(0).getExpiresAt());
        model.addAttribute("paymentMethods", PaymentMethod.values());
    }

    private boolean isAdmin(Authentication auth) {
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(RoleName.ROLE_ADMIN.name()));
    }
}
