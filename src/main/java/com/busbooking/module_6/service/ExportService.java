package com.busbooking.module_6.service;

import com.busbooking.module_6.dto.MonthlyReportDto;
import com.busbooking.module_6.dto.ReportFilterDto;
import com.busbooking.module_6.dto.RevenueStatisticDto;
import com.busbooking.module_6.dto.TicketSalesStatDto;
import com.busbooking.module_6.utils.ExcelExporter;
import com.busbooking.common.exception.BusinessException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {

    private final StatisticService statisticService;
    private final ExcelExporter excelExporter;

    public ExportService(StatisticService statisticService, ExcelExporter excelExporter) {
        this.statisticService = statisticService;
        this.excelExporter = excelExporter;
    }

    @Transactional(readOnly = true)
    public ResponseEntity<InputStreamResource> exportExcelReport(ReportFilterDto filter) {
        if (filter == null) {
            filter = new ReportFilterDto();
        }
        LocalDate today = LocalDate.now();
        LocalDate start = filter.getStartDate() != null ? filter.getStartDate() : today.minusDays(29);
        LocalDate end = filter.getEndDate() != null ? filter.getEndDate() : today;
        int year = filter.getYear() != null ? filter.getYear() : today.getYear();

        if (start.isAfter(end)) {
            throw new BusinessException("INVALID_DATE_RANGE", "Ngày bắt đầu không được lớn hơn ngày kết thúc.");
        }

        String reportType = filter.getReportType() != null ? filter.getReportType().toUpperCase() : "CUSTOM";
        List<RevenueStatisticDto> timeSeries;
        String title;

        if ("YEARLY".equals(reportType)) {
            timeSeries = statisticService.getYearlyRevenueStats(year - 4, year);
            title = "Báo Cáo Doanh Thu Theo Năm (" + (year - 4) + " - " + year + ")";
        } else {
            timeSeries = statisticService.getDailyRevenueStats(start, end, filter.getCompanyId(), filter.getRouteName());
            title = "Báo Cáo Doanh Thu Hệ Thống Đặt Vé Xe Khách";
        }

        List<MonthlyReportDto> monthlyStats = statisticService.getMonthlyRevenueBreakdown(year);
        List<TicketSalesStatDto> companyStats = statisticService.getSalesByCompany();
        List<TicketSalesStatDto> routeStats = statisticService.getSalesByRoute();

        try {
            ByteArrayInputStream stream = excelExporter.exportFullRevenueWorkbook(
                    title, start, end, timeSeries, monthlyStats, companyStats, routeStats
            );

            String fileName = "BaoCaoDoanhThu_" + today.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(stream));
        } catch (IOException ex) {
            throw new BusinessException("EXPORT_ERROR", "Không thể tạo file Excel báo cáo: " + ex.getMessage());
        }
    }
}
