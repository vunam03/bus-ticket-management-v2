package com.busbooking.module_6.service;

import com.busbooking.module_6.dto.MonthlyReportDto;
import com.busbooking.module_6.dto.ReportFilterDto;
import com.busbooking.module_6.dto.RevenueStatisticDto;
import com.busbooking.module_6.dto.TicketSalesStatDto;
import com.busbooking.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final StatisticService statisticService;

    public ReportService(StatisticService statisticService) {
        this.statisticService = statisticService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> generateRevenueReport(ReportFilterDto filter) {
        if (filter == null) {
            filter = new ReportFilterDto();
        }

        LocalDate today = LocalDate.now();
        if (filter.getStartDate() == null) {
            filter.setStartDate(today.minusDays(29));
        }
        if (filter.getEndDate() == null) {
            filter.setEndDate(today);
        }
        if (filter.getYear() == null) {
            filter.setYear(today.getYear());
        }

        if (filter.getStartDate().isAfter(filter.getEndDate())) {
            throw new BusinessException("INVALID_DATE_RANGE",
                    "Ngày bắt đầu không được lớn hơn ngày kết thúc. Vui lòng chọn lại khoảng ngày hợp lệ!");
        }

        List<RevenueStatisticDto> dailyStats = statisticService.getDailyRevenueStats(
                filter.getStartDate(),
                filter.getEndDate(),
                filter.getCompanyId(),
                filter.getRouteName()
        );

        BigDecimal periodRevenue = BigDecimal.ZERO;
        long periodTicketCount = 0;
        for (RevenueStatisticDto d : dailyStats) {
            periodRevenue = periodRevenue.add(d.getTotalRevenue());
            periodTicketCount += d.getTicketCount();
        }

        BigDecimal averageTicketPrice = periodTicketCount > 0
                ? periodRevenue.divide(BigDecimal.valueOf(periodTicketCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<MonthlyReportDto> monthlyStats = statisticService.getMonthlyRevenueBreakdown(filter.getYear());
        List<TicketSalesStatDto> companyStats = statisticService.getSalesByCompany();
        List<TicketSalesStatDto> routeStats = statisticService.getSalesByRoute();

        Map<String, Object> reportData = new LinkedHashMap<>();
        reportData.put("filter", filter);
        reportData.put("periodRevenue", periodRevenue);
        reportData.put("paidTicketCount", periodTicketCount);
        reportData.put("averageTicketPrice", averageTicketPrice);
        reportData.put("dailyStats", dailyStats);
        reportData.put("monthlyStats", monthlyStats);
        reportData.put("companyStats", companyStats);
        reportData.put("routeStats", routeStats);
        return reportData;
    }
}
