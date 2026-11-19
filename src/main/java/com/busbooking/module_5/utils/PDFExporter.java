package com.busbooking.module_5.utils;

import com.busbooking.common.utils.CurrencyUtils;
import com.busbooking.common.utils.DateTimeUtils;
import com.busbooking.module_5.model.Ticket;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

/**
 * Tiện ích tạo tài liệu vé điện tử PDF 1.4 chuẩn nhị phân không phụ thuộc thư viện ngoài (Module 5).
 */
@Component
public class PDFExporter {

    /**
     * Xuất thông tin vé xe khách sang mảng byte PDF 1.4 hợp lệ sẵn sàng tải xuống.
     */
    public byte[] exportTicketToPdf(Ticket ticket) {
        List<String> lines = buildTicketLines(ticket);

        StringBuilder contentStream = new StringBuilder();
        contentStream.append("BT\n/F1 11 Tf\n50 770 Td\n18 TL\n");
        for (String line : lines) {
            String escaped = line.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
            contentStream.append("(").append(escaped).append(") Tj T*\n");
        }
        contentStream.append("ET\n");

        String streamData = contentStream.toString();
        StringBuilder pdf = new StringBuilder();
        pdf.append("%PDF-1.4\n");
        int obj1 = pdf.length();
        pdf.append("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
        int obj2 = pdf.length();
        pdf.append("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");
        int obj3 = pdf.length();
        pdf.append("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n");
        int obj4 = pdf.length();
        pdf.append("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>\nendobj\n");
        int obj5 = pdf.length();
        pdf.append("5 0 obj\n<< /Length ").append(streamData.length()).append(" >>\nstream\n")
                .append(streamData).append("endstream\nendobj\n");
        int xref = pdf.length();
        pdf.append("xref\n0 6\n0000000000 65535 f \n");
        pdf.append(String.format("%010d 00000 n \n", obj1));
        pdf.append(String.format("%010d 00000 n \n", obj2));
        pdf.append(String.format("%010d 00000 n \n", obj3));
        pdf.append(String.format("%010d 00000 n \n", obj4));
        pdf.append(String.format("%010d 00000 n \n", obj5));
        pdf.append("trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n").append(xref).append("\n%%EOF");
        return pdf.toString().getBytes(StandardCharsets.US_ASCII);
    }

    private List<String> buildTicketLines(Ticket ticket) {
        List<String> lines = new ArrayList<>();
        lines.add("==========================================================");
        lines.add("        HE THONG DAT VE XE KHACH TRUC TUYEN BUSBOOKING    ");
        lines.add("                   THE LEN XE DIEN TU (E-TICKET)          ");
        lines.add("==========================================================");
        lines.add("Ma ve (Ticket Code):      " + safeAscii(ticket.getTicketCode()));
        lines.add("Trang thai ve:            " + safeAscii(ticket.getTicketStatus().name()));
        lines.add("Trang thai thanh toan:    " + safeAscii(ticket.getPaymentStatus().name()));
        lines.add("Phuong thuc thanh toan:   " + safeAscii(ticket.getPaymentMethod().name()));
        lines.add("----------------------------------------------------------");
        lines.add("THONG TIN HANH TRINH:");
        lines.add("Nha xe van hanh:          " + safeAscii(ticket.getBusTrip().getBusCompany().getName()));
        lines.add("Tuyen duong:              " + safeAscii(ticket.getBusTrip().getRouteName()));
        lines.add("Ben xuat phat:            " + safeAscii(ticket.getBusTrip().getDepartureStation() + " (" + ticket.getBusTrip().getDepartureProvince() + ")"));
        lines.add("Ben den:                  " + safeAscii(ticket.getBusTrip().getArrivalStation() + " (" + ticket.getBusTrip().getArrivalProvince() + ")"));
        lines.add("Gio khoi hanh:            " + DateTimeUtils.formatDateTime(ticket.getBusTrip().getDepartureTime()));
        lines.add("Bien so xe:               " + safeAscii(ticket.getBusTrip().getLicensePlate()));
        lines.add("So ghe / Giuong:          " + safeAscii(ticket.getSeat().getSeatNumber()) + " (Tang " + ticket.getSeat().getFloorNumber() + ")");
        lines.add("----------------------------------------------------------");
        lines.add("THONG TIN HANH KHACH:");
        lines.add("Ho va ten:                " + safeAscii(ticket.getPassengerName()));
        lines.add("So dien thoai:            " + safeAscii(ticket.getPassengerPhone()));
        lines.add("Email:                    " + safeAscii(ticket.getPassengerEmail()));
        lines.add("Ghi chu diem don:         " + safeAscii(ticket.getPickupNote()));
        lines.add("----------------------------------------------------------");
        lines.add("TONG TIEN VE:             " + safeAscii(CurrencyUtils.formatVnd(ticket.getTotalAmount())));
        lines.add("Ma xac thuc QR:           " + safeAscii(ticket.getQrCodePayload()));
        lines.add("Ngay xuat ve:             " + DateTimeUtils.formatDateTime(ticket.getCreatedAt()));
        lines.add("==========================================================");
        lines.add("Vui long co mat tai ben xe truoc gio khoi hanh 30 phut!");
        return lines;
    }

    /**
     * Loại bỏ dấu tiếng Việt để tương thích hoàn toàn với font chuẩn Courier của PDF 1.4.
     */
    public String safeAscii(String input) {
        if (input == null || input.isBlank()) {
            return "N/A";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd').replace('Đ', 'D')
                .replace("₫", "VND");
        return normalized.replaceAll("[^\\x20-\\x7E]", "");
    }
}
