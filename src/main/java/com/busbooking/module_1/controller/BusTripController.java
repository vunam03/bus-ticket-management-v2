package com.busbooking.module_1.controller;

import com.busbooking.common.enums.BusType;
import com.busbooking.common.enums.TripStatus;
import com.busbooking.module_1.dto.BusTripFormDto;
import com.busbooking.module_1.dto.BusTripRequest;
import com.busbooking.module_1.dto.BusTripResponse;
import com.busbooking.module_1.dto.BusTripSearchCriteria;
import com.busbooking.module_1.model.BusTrip;
import com.busbooking.module_1.service.BusCompanyService;
import com.busbooking.module_1.service.BusTripService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller bổ sung giao diện điều phối chuyên sâu (trips/trip-add/trip-edit) & API JSON quản lý chuyến xe (Module 1).
 */
@Controller
public class BusTripController {

    private final BusTripService tripService;
    private final BusCompanyService companyService;

    public BusTripController(BusTripService tripService, BusCompanyService companyService) {
        this.tripService = tripService;
        this.companyService = companyService;
    }

    @GetMapping("/admin/trips/board")
    public String viewTripDispatchBoard(@ModelAttribute("criteria") BusTripSearchCriteria criteria, Model model) {
        Page<BusTrip> pageResult = tripService.searchAdminTrips(criteria);
        model.addAttribute("tripPage", pageResult);
        model.addAttribute("trips", pageResult.getContent());
        model.addAttribute("companies", companyService.getAllCompanies());
        model.addAttribute("busTypes", BusType.values());
        model.addAttribute("tripStatuses", TripStatus.values());
        model.addAttribute("tripSummary", tripService.getAdminTripSummaryStats());
        model.addAttribute("activeMenu", "trips");
        return "module_1/trips";
    }

    @GetMapping("/admin/trips/add")
    public String viewAddTripPage(Model model) {
        BusTripFormDto form = new BusTripFormDto();
        form.setDepartureTime(LocalDateTime.now().plusDays(1).withHour(7).withMinute(30));
        form.setArrivalTime(LocalDateTime.now().plusDays(1).withHour(13).withMinute(30));
        form.setTicketPrice(new BigDecimal("280000"));
        form.setBusType(BusType.SLEEPER_40);

        model.addAttribute("tripForm", form);
        model.addAttribute("companies", companyService.getActiveCompanies());
        model.addAttribute("busTypes", BusType.values());
        model.addAttribute("tripStatuses", TripStatus.values());
        model.addAttribute("activeMenu", "trip-create");
        return "module_1/trip-add";
    }

    @GetMapping("/admin/trips/{id}/edit-panel")
    public String viewEditTripPage(@PathVariable Long id, Model model) {
        BusTrip trip = tripService.getTripById(id);
        model.addAttribute("trip", trip);
        model.addAttribute("tripForm", tripService.toFormDto(trip));
        model.addAttribute("companies", companyService.getAllCompanies());
        model.addAttribute("busTypes", BusType.values());
        model.addAttribute("tripStatuses", TripStatus.values());
        model.addAttribute("activeMenu", "trips");
        return "module_1/trip-edit";
    }

    @GetMapping("/api/admin/trips")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiSearchTrips(@ModelAttribute BusTripSearchCriteria criteria) {
        Page<BusTrip> pageResult = tripService.searchAdminTrips(criteria);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("content", pageResult.getContent().stream().map(BusTripResponse::fromEntity).toList());
        body.put("page", pageResult.getNumber());
        body.put("size", pageResult.getSize());
        body.put("totalElements", pageResult.getTotalElements());
        body.put("totalPages", pageResult.getTotalPages());
        body.put("summary", tripService.getAdminTripSummaryStats());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/api/admin/trips/{id}")
    @ResponseBody
    public ResponseEntity<BusTripResponse> apiGetTripDetail(@PathVariable Long id) {
        return ResponseEntity.ok(tripService.getTripResponseById(id));
    }

    @PostMapping("/api/admin/trips")
    @ResponseBody
    public ResponseEntity<BusTripResponse> apiCreateTrip(@Valid @RequestBody BusTripRequest request) {
        BusTrip created = tripService.createTrip(request);
        return ResponseEntity.ok(BusTripResponse.fromEntity(created));
    }

    @PatchMapping("/api/admin/trips/{id}/status")
    @ResponseBody
    public ResponseEntity<BusTripResponse> apiUpdateTripStatus(@PathVariable Long id,
                                                               @RequestParam TripStatus status) {
        BusTrip updated = tripService.updateTripStatus(id, status);
        return ResponseEntity.ok(BusTripResponse.fromEntity(updated));
    }
}
