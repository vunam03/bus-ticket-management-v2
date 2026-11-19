package com.busbooking.module_6.controller;

import com.busbooking.module_1.service.BusCompanyService;
import com.busbooking.module_6.dto.ReportFilterDto;
import com.busbooking.module_6.repository.DashboardTripRepository;
import com.busbooking.module_6.service.ExportService;
import com.busbooking.module_6.service.ReportService;
import com.busbooking.common.exception.BusinessException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/reports")
public class ReportController {

    private final ReportService reportService;
    private final ExportService exportService;
    private final BusCompanyService companyService;
    private final DashboardTripRepository tripRepository;

    public ReportController(ReportService reportService,
                            ExportService exportService,
                            BusCompanyService companyService,
                            DashboardTripRepository tripRepository) {
        this.reportService = reportService;
        this.exportService = exportService;
        this.companyService = companyService;
        this.tripRepository = tripRepository;
    }

    @GetMapping({"/revenue", ""})
    public String showRevenueReport(@ModelAttribute("filter") ReportFilterDto filter, Model model) {
        int currentYear = LocalDate.now().getYear();
        try {
            Map<String, Object> reportData = reportService.generateRevenueReport(filter);
            model.addAllAttributes(reportData);
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            ReportFilterDto fallback = new ReportFilterDto();
            model.addAllAttributes(reportService.generateRevenueReport(fallback));
        }

        model.addAttribute("companies", companyService.getAllCompanies());
        model.addAttribute("routes", tripRepository.findAllDistinctRoutes());
        model.addAttribute("availableYears", List.of(currentYear, currentYear - 1, currentYear - 2));
        model.addAttribute("activeMenu", "reports");
        return "module_6/revenue-report";
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportReportExcel(@ModelAttribute ReportFilterDto filter) {
        return exportService.exportExcelReport(filter);
    }
}
