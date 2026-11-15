package com.busbooking.module_1.controller;

import com.busbooking.common.enums.BusType;
import com.busbooking.common.enums.TripStatus;
import com.busbooking.common.exception.BusinessException;
import com.busbooking.module_1.dto.BusTripFormDto;
import com.busbooking.module_1.dto.BusTripSearchCriteria;
import com.busbooking.module_1.model.BusTrip;
import com.busbooking.module_1.service.BusCompanyService;
import com.busbooking.module_1.service.BusTripService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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
    public String listTrips(@ModelAttribute("criteria") BusTripSearchCriteria criteria, Model model) {
        if (criteria != null && criteria.hasAnyFilter()) {
            Page<BusTrip> tripPage = tripService.searchAdminTrips(criteria);
            model.addAttribute("trips", tripPage.getContent());
            model.addAttribute("tripPage", tripPage);
        } else {
            model.addAttribute("trips", tripService.getAllTrips());
        }
        model.addAttribute("companies", companyService.getAllCompanies());
        model.addAttribute("tripStatuses", TripStatus.values());
        model.addAttribute("busTypes", BusType.values());
        model.addAttribute("tripSummary", tripService.getAdminTripSummaryStats());
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
        BusTripFormDto form = tripService.toFormDto(trip);

        populateFormReferenceData(model, form, true);
        model.addAttribute("tripCode", trip.getTripCode());
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

    @PostMapping("/{id}/duplicate")
    public String duplicateTrip(@PathVariable Long id,
                                @RequestParam(defaultValue = "1") int plusDays,
                                RedirectAttributes redirectAttributes) {
        try {
            BusTrip cloned = tripService.duplicateTrip(id, plusDays);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã nhân bản thành công chuyến xe mới " + cloned.getTripCode() + " khởi hành sau +" + plusDays + " ngày!");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/trips";
    }

    @PostMapping("/{id}/status")
    public String changeTripStatus(@PathVariable Long id,
                                   @RequestParam TripStatus status,
                                   RedirectAttributes redirectAttributes) {
        BusTrip updated = tripService.updateTripStatus(id, status);
        redirectAttributes.addFlashAttribute("successMessage",
                "Đã chuyển trạng thái chuyến " + updated.getTripCode() + " sang '" + updated.getStatus().getDisplayName() + "'.");
        return "redirect:/admin/trips";
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
