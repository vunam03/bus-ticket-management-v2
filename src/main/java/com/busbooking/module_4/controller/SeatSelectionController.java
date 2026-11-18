package com.busbooking.module_4.controller;

import com.busbooking.common.exception.BusinessException;
import com.busbooking.module_1.model.BusTrip;
import com.busbooking.module_1.service.BusTripService;
import com.busbooking.module_4.dto.SeatHoldResultDto;
import com.busbooking.module_4.dto.SeatLayoutResponse;
import com.busbooking.module_4.model.Seat;
import com.busbooking.module_4.service.SeatSelectionService;
import com.busbooking.module_4.service.SeatService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller điều hướng giao diện sơ đồ chọn ghế và xử lý giữ/hủy ghế qua form MVC (Module 4).
 */
@Controller
@RequestMapping("/booking")
public class SeatSelectionController {

    private final SeatSelectionService seatSelectionService;
    private final SeatService seatService;
    private final BusTripService busTripService;

    public SeatSelectionController(SeatSelectionService seatSelectionService,
                                   SeatService seatService,
                                   BusTripService busTripService) {
        this.seatSelectionService = seatSelectionService;
        this.seatService = seatService;
        this.busTripService = busTripService;
    }

    @GetMapping("/trip/{tripId}/seats")
    public String showSeatDiagram(@PathVariable Long tripId, Authentication auth, Model model) {
        String username = extractAuthenticatedUsername(auth);
        BusTrip trip = busTripService.getTripById(tripId);
        List<Seat> allSeats = seatSelectionService.getSeatsForTrip(tripId);
        SeatLayoutResponse seatLayout = seatService.getSeatLayoutForTrip(tripId, username);

        Map<Integer, List<Seat>> seatsByFloor = allSeats.stream()
                .collect(Collectors.groupingBy(Seat::getFloorNumber));

        model.addAttribute("trip", trip);
        model.addAttribute("allSeats", allSeats);
        model.addAttribute("floor1Seats", seatsByFloor.getOrDefault(1, List.of()));
        model.addAttribute("floor2Seats", seatsByFloor.getOrDefault(2, List.of()));
        model.addAttribute("hasTwoFloors", trip.getBusType().getFloors() == 2);
        model.addAttribute("seatLayout", seatLayout);
        return "module_4/seat-selection";
    }

    @PostMapping("/trip/{tripId}/hold")
    public String holdSelectedSeats(@PathVariable Long tripId,
                                    @RequestParam(value = "seatIds", required = false) List<Long> seatIds,
                                    Authentication auth,
                                    RedirectAttributes redirectAttributes) {
        try {
            SeatHoldResultDto holdResult = seatSelectionService.holdSeats(tripId, seatIds, auth.getName());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã giữ chỗ tạm thời ghế " + String.join(", ", holdResult.getSeatNumbers()) + " trong 10 phút!");
            return "redirect:/booking/checkout?holdCode=" + holdResult.getHoldCode();
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/booking/trip/" + tripId + "/seats";
        }
    }

    @PostMapping("/hold/release/{holdCode}")
    public String releaseSeatHold(@PathVariable String holdCode,
                                  @RequestParam("tripId") Long tripId,
                                  Authentication auth,
                                  RedirectAttributes redirectAttributes) {
        try {
            seatSelectionService.releaseHoldByCode(holdCode, auth.getName());
            redirectAttributes.addFlashAttribute("warningMessage", "Đã hủy giữ ghế và trả ghế về trạng thái trống.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/booking/trip/" + tripId + "/seats";
    }

    private String extractAuthenticatedUsername(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        String name = auth.getName();
        return (name == null || name.isBlank() || "anonymousUser".equalsIgnoreCase(name)) ? null : name;
    }
}
