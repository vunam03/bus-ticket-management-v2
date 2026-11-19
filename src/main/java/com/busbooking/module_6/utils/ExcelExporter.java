package com.busbooking.module_6.utils;

import com.busbooking.module_6.dto.MonthlyReportDto;
import com.busbooking.module_6.dto.RevenueStatisticDto;
import com.busbooking.module_6.dto.TicketSalesStatDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Module 6 – Tiện ích xuất file Excel (.xlsx) chuyên nghiệp bằng Apache POI 5.4.1.
 * Hỗ trợ báo cáo Ngày, Tháng, Năm, Tùy chỉnh kèm bảng tổng hợp Nhà xe và Tuyến đường.
 */
@Component
public class ExcelExporter {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    public ByteArrayInputStream exportFullRevenueWorkbook(String reportTitle,
                                                          LocalDate startDate,
                                                          LocalDate endDate,
                                                          List<RevenueStatisticDto> timeSeriesStats,
                                                          List<MonthlyReportDto> monthlyStats,
                                                          List<TicketSalesStatDto> companyStats,
                                                          List<TicketSalesStatDto> routeStats) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle subTitleStyle = createSubTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle centerStyle = createCenterDataStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle totalLabelStyle = createTotalLabelStyle(workbook);
            CellStyle totalCurrencyStyle = createTotalCurrencyStyle(workbook);

            // Sheet 1: Báo cáo doanh thu chi tiết theo kỳ
            Sheet sheet1 = workbook.createSheet("Doanh Thu Theo Ky");
            buildTimeSeriesSheet(sheet1, reportTitle, startDate, endDate, timeSeriesStats,
                    titleStyle, subTitleStyle, headerStyle, centerStyle, dataStyle, currencyStyle, totalLabelStyle, totalCurrencyStyle);

            // Sheet 2: Báo cáo doanh thu 12 tháng
            if (monthlyStats != null && !monthlyStats.isEmpty()) {
                Sheet sheet2 = workbook.createSheet("Doanh Thu 12 Thang");
                buildMonthlySheet(sheet2, monthlyStats,
                        titleStyle, subTitleStyle, headerStyle, centerStyle, dataStyle, currencyStyle, totalLabelStyle, totalCurrencyStyle);
            }

            // Sheet 3: Doanh thu theo Nhà xe & Tuyến đường
            Sheet sheet3 = workbook.createSheet("Nha Xe Va Tuyen Duong");
            buildDimensionSheet(sheet3, companyStats, routeStats,
                    titleStyle, headerStyle, centerStyle, dataStyle, currencyStyle);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private void buildTimeSeriesSheet(Sheet sheet,
                                      String reportTitle,
                                      LocalDate startDate,
                                      LocalDate endDate,
                                      List<RevenueStatisticDto> stats,
                                      CellStyle titleStyle,
                                      CellStyle subTitleStyle,
                                      CellStyle headerStyle,
                                      CellStyle centerStyle,
                                      CellStyle dataStyle,
                                      CellStyle currencyStyle,
                                      CellStyle totalLabelStyle,
                                      CellStyle totalCurrencyStyle) {
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(28);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(reportTitle.toUpperCase());
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        Row subRow = sheet.createRow(1);
        Cell subCell = subRow.createCell(0);
        String rangeInfo = (startDate != null && endDate != null)
                ? "Kỳ báo cáo: " + startDate.format(DATE_FMT) + " - " + endDate.format(DATE_FMT)
                : "Thời gian xuất: " + LocalDateTime.now().format(TIMESTAMP_FMT);
        subCell.setCellValue(rangeInfo + " | Đơn vị tiền tệ: VNĐ");
        subCell.setCellStyle(subTitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));

        String[] headers = {"STT", "Mốc Thời Gian", "Số Vé Đã Bán", "Giá Vé Trung Bình (VNĐ)", "Tổng Doanh Thu (VNĐ)"};
        Row headerRow = sheet.createRow(3);
        headerRow.setHeightInPoints(24);
        for (int i = 0; i < headers.length; i++) {
            Cell c = headerRow.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(headerStyle);
        }

        int rowIdx = 4;
        int stt = 1;
        long totalTickets = 0;
        BigDecimal grandTotal = BigDecimal.ZERO;

        if (stats != null) {
            for (RevenueStatisticDto item : stats) {
                Row row = sheet.createRow(rowIdx++);
                Cell c0 = row.createCell(0);
                c0.setCellValue(stt++);
                c0.setCellStyle(centerStyle);

                Cell c1 = row.createCell(1);
                c1.setCellValue(item.getPeriodLabel());
                c1.setCellStyle(dataStyle);

                Cell c2 = row.createCell(2);
                c2.setCellValue(item.getTicketCount());
                c2.setCellStyle(centerStyle);

                Cell c3 = row.createCell(3);
                c3.setCellValue(item.getAverageTicketPrice() != null ? item.getAverageTicketPrice().doubleValue() : 0.0);
                c3.setCellStyle(currencyStyle);

                Cell c4 = row.createCell(4);
                c4.setCellValue(item.getTotalRevenue() != null ? item.getTotalRevenue().doubleValue() : 0.0);
                c4.setCellStyle(currencyStyle);

                totalTickets += item.getTicketCount();
                if (item.getTotalRevenue() != null) {
                    grandTotal = grandTotal.add(item.getTotalRevenue());
                }
            }
        }

        Row totalRow = sheet.createRow(rowIdx);
        totalRow.setHeightInPoints(22);
        Cell tc0 = totalRow.createCell(0);
        tc0.setCellValue("TỔNG CỘNG");
        tc0.setCellStyle(totalLabelStyle);
        Cell tc1 = totalRow.createCell(1);
        tc1.setCellStyle(totalLabelStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 1));

        Cell tc2 = totalRow.createCell(2);
        tc2.setCellValue(totalTickets);
        tc2.setCellStyle(totalLabelStyle);

        Cell tc3 = totalRow.createCell(3);
        tc3.setCellValue("");
        tc3.setCellStyle(totalLabelStyle);

        Cell tc4 = totalRow.createCell(4);
        tc4.setCellValue(grandTotal.doubleValue());
        tc4.setCellStyle(totalCurrencyStyle);

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i) + 1200, 4200));
        }
    }

    private void buildMonthlySheet(Sheet sheet,
                                   List<MonthlyReportDto> monthlyStats,
                                   CellStyle titleStyle,
                                   CellStyle subTitleStyle,
                                   CellStyle headerStyle,
                                   CellStyle centerStyle,
                                   CellStyle dataStyle,
                                   CellStyle currencyStyle,
                                   CellStyle totalLabelStyle,
                                   CellStyle totalCurrencyStyle) {
        int year = monthlyStats.get(0).getYear();
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(26);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("BÁO CÁO DOANH THU 12 THÁNG NĂM " + year);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        Row subRow = sheet.createRow(1);
        Cell subCell = subRow.createCell(0);
        subCell.setCellValue("Thống kê vé đã thanh toán (CONFIRMED + PAID) theo từng tháng");
        subCell.setCellStyle(subTitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));

        String[] headers = {"Tháng", "Số Vé Đã Bán", "Số Vé Đã Hủy", "Tăng Trưởng (%)", "Doanh Thu (VNĐ)"};
        Row headerRow = sheet.createRow(3);
        headerRow.setHeightInPoints(24);
        for (int i = 0; i < headers.length; i++) {
            Cell c = headerRow.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(headerStyle);
        }

        int rowIdx = 4;
        long sumPaid = 0;
        long sumCancelled = 0;
        BigDecimal sumRev = BigDecimal.ZERO;

        for (MonthlyReportDto m : monthlyStats) {
            Row row = sheet.createRow(rowIdx++);
            Cell c0 = row.createCell(0);
            c0.setCellValue(m.getMonthLabel());
            c0.setCellStyle(centerStyle);

            Cell c1 = row.createCell(1);
            c1.setCellValue(m.getPaidTickets());
            c1.setCellStyle(centerStyle);

            Cell c2 = row.createCell(2);
            c2.setCellValue(m.getCancelledTickets());
            c2.setCellStyle(centerStyle);

            Cell c3 = row.createCell(3);
            c3.setCellValue(m.getGrowthRate() + "%");
            c3.setCellStyle(centerStyle);

            Cell c4 = row.createCell(4);
            c4.setCellValue(m.getRevenue().doubleValue());
            c4.setCellStyle(currencyStyle);

            sumPaid += m.getPaidTickets();
            sumCancelled += m.getCancelledTickets();
            sumRev = sumRev.add(m.getRevenue());
        }

        Row totalRow = sheet.createRow(rowIdx);
        totalRow.setHeightInPoints(22);
        Cell t0 = totalRow.createCell(0);
        t0.setCellValue("CẢ NĂM " + year);
        t0.setCellStyle(totalLabelStyle);

        Cell t1 = totalRow.createCell(1);
        t1.setCellValue(sumPaid);
        t1.setCellStyle(totalLabelStyle);

        Cell t2 = totalRow.createCell(2);
        t2.setCellValue(sumCancelled);
        t2.setCellStyle(totalLabelStyle);

        Cell t3 = totalRow.createCell(3);
        t3.setCellValue("-");
        t3.setCellStyle(totalLabelStyle);

        Cell t4 = totalRow.createCell(4);
        t4.setCellValue(sumRev.doubleValue());
        t4.setCellStyle(totalCurrencyStyle);

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i) + 1200, 4200));
        }
    }

    private void buildDimensionSheet(Sheet sheet,
                                     List<TicketSalesStatDto> companyStats,
                                     List<TicketSalesStatDto> routeStats,
                                     CellStyle titleStyle,
                                     CellStyle headerStyle,
                                     CellStyle centerStyle,
                                     CellStyle dataStyle,
                                     CellStyle currencyStyle) {
        Row title1 = sheet.createRow(0);
        title1.setHeightInPoints(24);
        Cell tc1 = title1.createCell(0);
        tc1.setCellValue("THỐNG KÊ DOANH THU THEO NHÀ XE");
        tc1.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

        String[] compHeaders = {"STT", "Tên Nhà Xe", "Hotline", "Số Vé Đã Bán", "Doanh Thu (VNĐ)"};
        Row hRow1 = sheet.createRow(2);
        for (int i = 0; i < compHeaders.length; i++) {
            Cell c = hRow1.createCell(i);
            c.setCellValue(compHeaders[i]);
            c.setCellStyle(headerStyle);
        }

        int rowIdx = 3;
        int stt = 1;
        if (companyStats != null) {
            for (TicketSalesStatDto item : companyStats) {
                Row r = sheet.createRow(rowIdx++);
                Cell c0 = r.createCell(0);
                c0.setCellValue(stt++);
                c0.setCellStyle(centerStyle);

                Cell c1 = r.createCell(1);
                c1.setCellValue(item.getDimensionName());
                c1.setCellStyle(dataStyle);

                Cell c2 = r.createCell(2);
                c2.setCellValue(item.getSecondaryInfo());
                c2.setCellStyle(centerStyle);

                Cell c3 = r.createCell(3);
                c3.setCellValue(item.getTicketsSold());
                c3.setCellStyle(centerStyle);

                Cell c4 = r.createCell(4);
                c4.setCellValue(item.getRevenue().doubleValue());
                c4.setCellStyle(currencyStyle);
            }
        }

        rowIdx += 2;
        Row title2 = sheet.createRow(rowIdx++);
        title2.setHeightInPoints(24);
        Cell tc2 = title2.createCell(0);
        tc2.setCellValue("THỐNG KÊ DOANH THU THEO TUYẾN ĐƯỜNG");
        tc2.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(title2.getRowNum(), title2.getRowNum(), 0, 4));

        String[] routeHeaders = {"STT", "Tuyến Đường", "Bến Xuất Phát", "Số Vé Đã Bán", "Doanh Thu (VNĐ)"};
        Row hRow2 = sheet.createRow(rowIdx++);
        for (int i = 0; i < routeHeaders.length; i++) {
            Cell c = hRow2.createCell(i);
            c.setCellValue(routeHeaders[i]);
            c.setCellStyle(headerStyle);
        }

        stt = 1;
        if (routeStats != null) {
            for (TicketSalesStatDto item : routeStats) {
                Row r = sheet.createRow(rowIdx++);
                Cell c0 = r.createCell(0);
                c0.setCellValue(stt++);
                c0.setCellStyle(centerStyle);

                Cell c1 = r.createCell(1);
                c1.setCellValue(item.getDimensionName());
                c1.setCellStyle(dataStyle);

                Cell c2 = r.createCell(2);
                c2.setCellValue(item.getSecondaryInfo());
                c2.setCellStyle(dataStyle);

                Cell c3 = r.createCell(3);
                c3.setCellValue(item.getTicketsSold());
                c3.setCellStyle(centerStyle);

                Cell c4 = r.createCell(4);
                c4.setCellValue(item.getRevenue().doubleValue());
                c4.setCellStyle(currencyStyle);
            }
        }

        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i) + 1200, 4200));
        }
    }

    private CellStyle createTitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("Segoe UI");
        font.setFontHeightInPoints((short) 14);
        font.setBold(true);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createSubTitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("Segoe UI");
        font.setFontHeightInPoints((short) 10);
        font.setItalic(true);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("Segoe UI");
        font.setFontHeightInPoints((short) 11);
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    private CellStyle createDataStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("Segoe UI");
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    private CellStyle createCenterDataStyle(Workbook wb) {
        CellStyle style = createDataStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook wb) {
        CellStyle style = createDataStyle(wb);
        DataFormat format = wb.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0\" ₫\""));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private CellStyle createTotalLabelStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("Segoe UI");
        font.setFontHeightInPoints((short) 11);
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    private CellStyle createTotalCurrencyStyle(Workbook wb) {
        CellStyle style = createTotalLabelStyle(wb);
        DataFormat format = wb.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0\" ₫\""));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private void setBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
