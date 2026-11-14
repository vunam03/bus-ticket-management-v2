package com.busbooking.module_1.controller;

import com.busbooking.module_1.dto.BusTripFormDto;
import com.busbooking.module_1.service.BusCompanyService;
import com.busbooking.module_1.service.BusTripService;
import com.busbooking.shared.enums.BusType;
import com.busbooking.shared.enums.TripStatus;
import com.busbooking.shared.exception.BusinessException;
import com.busbooking.shared.model.BusTrip;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/trips")
public class AdminTripController {

    private final BusTripService tripService;
    private final BusCompanyService companyService;

    public AdminTripController(BusTripService tripService, BusCompanyService companyService) {
        this.tripService = tripService;
        this.companyService = companyService;
    }

    @GetMapping
    public String listTrips(Model model) {
        model.addAttribute("trips", tripService.getAllTrips());
        model.addAttribute("activeMenu", "trips");
        return "module_1/trip-list";
    }

    @GetMapping({"/create", "/new"})
    public String showCreateForm(Model model) {
        BusTripFormDto form = new BusTripFormDto();
        form.setDepartureTime(LocalDateTime.now().plusDays(1).withHour(8).withMinute(0));
        form.setArrivalTime(LocalDateTime.now().plusDays(1).withHour(14).withMinute(0));
        form.setTicketPrice(new BigDecimal("250000"));
        form.setBusType(BusType.SLEEPER_40);

        populateFormReferenceData(model, form, false);
        return "module_1/trip-form";
    }

    @GetMapping({"/edit/{id}", "/{id}/edit"})
    public String showEditForm(@PathVariable Long id, Model model) {
        BusTrip trip = tripService.getTripById(id);
        BusTripFormDto form = new BusTripFormDto();
        form.setId(trip.getId());
        form.setCompanyId(trip.getBusCompany().getId());
        form.setDepartureProvince(trip.getDepartureProvince());
        form.setDepartureStation(trip.getDepartureStation());
        form.setArrivalProvince(trip.getArrivalProvince());
        form.setArrivalStation(trip.getArrivalStation());
        form.setDepartureTime(trip.getDepartureTime());
        form.setArrivalTime(trip.getArrivalTime());
        form.setTicketPrice(trip.getTicketPrice());
        form.setBusType(trip.getBusType());
        form.setLicensePlate(trip.getLicensePlate());
        form.setAmenities(trip.getAmenities());
        form.setStatus(trip.getStatus());

        populateFormReferenceData(model, form, true);
        return "module_1/trip-form";
    }

    @PostMapping({"/save", "/new", "/{id}/edit"})
    public String saveTrip(@Valid @ModelAttribute("tripForm") BusTripFormDto form,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        boolean isEdit = form.getId() != null;
        if (bindingResult.hasErrors()) {
            populateFormReferenceData(model, form, isEdit);
            return "module_1/trip-form";
        }

        try {
            if (!isEdit) {
                BusTrip created = tripService.createTrip(form);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã thêm chuyến xe " + created.getTripCode() + " và tự động tạo sơ đồ " + created.getTotalSeats() + " ghế!");
            } else {
                BusTrip updated = tripService.updateTrip(form.getId(), form);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã cập nhật thông tin chuyến xe " + updated.getTripCode() + " thành công!");
            }
            return "redirect:/admin/trips";
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            populateFormReferenceData(model, form, isEdit);
            return "module_1/trip-form";
        }
    }

    @PostMapping({"/cancel/{id}", "/{id}/cancel"})
    public String cancelTrip(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        BusTrip cancelled = tripService.cancelTrip(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "Đã hủy chuyến xe " + cancelled.getTripCode() + ". Dữ liệu vé của hành khách vẫn được giữ nguyên để đối soát.");
        return "redirect:/admin/trips";
    }

    @PostMapping({"/delete/{id}", "/{id}/delete"})
    public String deleteTrip(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean deletedHard = tripService.deleteOrCancelTrip(id);
        if (deletedHard) {
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa hoàn toàn chuyến xe chưa phát sinh vé.");
        } else {
            redirectAttributes.addFlashAttribute("warningMessage",
                    "Chuyến xe đã có khách đặt vé nên được chuyển sang trạng thái 'Đã hủy chuyến' để bảo toàn dữ liệu vé.");
        }
        return "redirect:/admin/trips";
    }

    private void populateFormReferenceData(Model model, BusTripFormDto form, boolean editMode) {
        model.addAttribute("tripForm", form);
        model.addAttribute("companies", companyService.getAllCompanies());
        model.addAttribute("busTypes", BusType.values());
        model.addAttribute("tripStatuses", TripStatus.values());
        model.addAttribute("statuses", TripStatus.values());
        model.addAttribute("editMode", editMode);
        model.addAttribute("activeMenu", editMode ? "trips" : "trip-create");
    }
}
