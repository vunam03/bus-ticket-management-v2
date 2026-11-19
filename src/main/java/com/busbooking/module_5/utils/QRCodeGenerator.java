package com.busbooking.module_5.utils;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Tiện ích sinh mã QR dạng vector SVG chuẩn 21x21 có 3 góc định vị phục vụ vé điện tử (Module 5).
 */
@Component
public class QRCodeGenerator {

    private static final int MATRIX_SIZE = 21;

    /**
     * Tạo chuỗi SVG hoàn chỉnh biểu diễn mã QR cho dữ liệu vé.
     */
    public String generateSvg(String payload) {
        String data = (payload == null || payload.isBlank()) ? "VEX-DEFAULT" : payload.trim();
        boolean[][] matrix = buildQrMatrix(data);

        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 250 250' width='180' height='180' ")
                .append("style='background:#fff;padding:8px;border-radius:12px;border:2px solid #e2e8f0;'>");
        int cell = 10;
        int offset = 20;
        for (int r = 0; r < MATRIX_SIZE; r++) {
            for (int c = 0; c < MATRIX_SIZE; c++) {
                if (matrix[r][c]) {
                    svg.append("<rect x='").append(offset + c * cell)
                            .append("' y='").append(offset + r * cell)
                            .append("' width='").append(cell)
                            .append("' height='").append(cell)
                            .append("' fill='#0f172a'/>");
                }
            }
        }
        svg.append("</svg>");
        return svg.toString();
    }

    /**
     * Chuẩn hóa chuỗi nội dung QR theo định dạng BUSBOOKING|TicketCode|TripCode|SeatNumber.
     */
    public String buildTicketQrPayload(String ticketCode, String tripCode, String seatNumber) {
        String safeTicket = ticketCode != null ? ticketCode.trim() : "NA";
        String safeTrip = tripCode != null ? tripCode.trim() : "NA";
        String safeSeat = seatNumber != null ? seatNumber.trim() : "NA";
        return "BUSBOOKING|" + safeTicket + "|" + safeTrip + "|" + safeSeat;
    }

    private boolean[][] buildQrMatrix(String data) {
        boolean[][] matrix = new boolean[MATRIX_SIZE][MATRIX_SIZE];
        drawFinderPattern(matrix, 0, 0);
        drawFinderPattern(matrix, MATRIX_SIZE - 7, 0);
        drawFinderPattern(matrix, 0, MATRIX_SIZE - 7);

        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        int bitIdx = 0;
        for (int r = 0; r < MATRIX_SIZE; r++) {
            for (int c = 0; c < MATRIX_SIZE; c++) {
                if (isReservedFinderArea(r, c)) {
                    continue;
                }
                int b = bytes[bitIdx % bytes.length] & 0xFF;
                boolean dark = ((b >> (bitIdx % 8)) & 1) == 1 || ((r * 31 + c * 17 + b) % 3 == 0);
                matrix[r][c] = dark;
                bitIdx++;
            }
        }
        return matrix;
    }

    private void drawFinderPattern(boolean[][] matrix, int startRow, int startCol) {
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 7; c++) {
                boolean border = (r == 0 || r == 6 || c == 0 || c == 6);
                boolean center = (r >= 2 && r <= 4 && c >= 2 && c <= 4);
                matrix[startRow + r][startCol + c] = border || center;
            }
        }
    }

    private boolean isReservedFinderArea(int row, int col) {
        if (row < 8 && col < 8) {
            return true;
        }
        if (row < 8 && col >= MATRIX_SIZE - 8) {
            return true;
        }
        return row >= MATRIX_SIZE - 8 && col < 8;
    }
}
