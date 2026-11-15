package com.busbooking.module_1.controller;

import com.busbooking.common.exception.BusinessException;
import com.busbooking.module_1.dto.BusCompanyFormDto;
import com.busbooking.module_1.model.BusCompany;
import com.busbooking.module_1.service.BusCompanyService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller mở rộng cho hồ sơ đối tác nhà xe (bus-companies.html) & API JSON quản lý nhà xe (Module 1).
 */
@Controller
public class BusCompanyController {

    private final BusCompanyService companyService;

    public BusCompanyController(BusCompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("/admin/companies/manage")
    public String viewCompanyManagementBoard(@RequestParam(required = false) String keyword, Model model) {
        List<BusCompany> companies = companyService.searchCompanies(keyword);
        model.addAttribute("companies", companies);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("companyForm", new BusCompanyFormDto());
        model.addAttribute("tripCountMap", companyService.getTripCountByCompany());
        model.addAttribute("overviewStats", companyService.getCompanyOverviewStats());
        model.addAttribute("activeMenu", "companies");
        return "module_1/bus-companies";
    }

    @PostMapping("/admin/companies/{id}/delete")
    public String deleteUnusedCompany(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            companyService.deleteCompanyIfUnused(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa nhà xe chưa phát sinh chuyến khỏi hệ thống.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("warningMessage", ex.getMessage());
        }
        return "redirect:/admin/companies/manage";
    }

    @GetMapping("/api/admin/companies")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiListCompanies(@RequestParam(required = false) String keyword) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("companies", companyService.searchCompanies(keyword));
        body.put("overview", companyService.getCompanyOverviewStats());
        body.put("tripCounts", companyService.getTripCountByCompany());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/api/admin/companies/{id}/toggle")
    @ResponseBody
    public ResponseEntity<BusCompany> apiToggleCompanyStatus(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.toggleCompanyStatus(id));
    }
}
