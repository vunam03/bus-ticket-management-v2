package com.busbooking.module_6.controller;

import com.busbooking.module_6.dto.CustomerAnalyticsDto;
import com.busbooking.module_6.dto.DashboardSummaryDto;
import com.busbooking.module_6.dto.MonthlyReportDto;
import com.busbooking.module_6.service.StatisticService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    private final StatisticService statisticService;

    public AdminDashboardController(StatisticService statisticService) {
        this.statisticService = statisticService;
    }

    @GetMapping
    public String showDashboard(@RequestParam(value = "year", required = false) Integer year, Model model) {
        int currentYear = LocalDate.now().getYear();
        int selectedYear = (year != null) ? year : currentYear;

        DashboardSummaryDto summary = statisticService.getDashboardSummary(selectedYear);
        model.addAttribute("summary", summary);
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("availableYears", List.of(currentYear, currentYear - 1, currentYear - 2));
        model.addAttribute("activeMenu", "dashboard");
        return "module_6/dashboard";
    }

    @GetMapping("/customers")
    public String showCustomerAnalytics(@RequestParam(value = "keyword", required = false) String keyword,
                                        @RequestParam(value = "page", defaultValue = "0") int page,
                                        @RequestParam(value = "size", defaultValue = "10") int size,
                                        Model model) {
        Page<CustomerAnalyticsDto> customerPage = statisticService.getCustomerAnalyticsPage(
                keyword, PageRequest.of(Math.max(0, page), size));

        model.addAttribute("customerPage", customerPage);
        model.addAttribute("customers", customerPage.getContent());
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", customerPage.getTotalPages());
        model.addAttribute("activeMenu", "customers");
        return "module_6/customer-list";
    }

    @GetMapping("/api/revenue-chart")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getYearlyRevenueChartApi(@RequestParam("year") int year) {
        List<MonthlyReportDto> monthly = statisticService.getMonthlyRevenueBreakdown(year);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("year", year);
        response.put("labels", monthly.stream().map(MonthlyReportDto::getMonthLabel).collect(Collectors.toList()));
        response.put("revenues", monthly.stream().map(MonthlyReportDto::getRevenue).collect(Collectors.toList()));
        response.put("paidTickets", monthly.stream().map(MonthlyReportDto::getPaidTickets).collect(Collectors.toList()));
        return ResponseEntity.ok(response);
    }
}
