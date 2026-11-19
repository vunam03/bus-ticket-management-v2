package com.busbooking.module_5.service;

import com.busbooking.module_5.model.Ticket;
import com.busbooking.module_5.utils.PDFExporter;
import com.busbooking.module_5.utils.QRCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Dịch vụ tạo mã QR SVG trực quan và xuất file vé PDF chuẩn (%PDF-1.4) thông qua QRCodeGenerator và PDFExporter (Module 5).
 */
@Service
public class QrCodeAndPdfService {

    private final QRCodeGenerator qrCodeGenerator;
    private final PDFExporter pdfExporter;

    public QrCodeAndPdfService() {
        this(new QRCodeGenerator(), new PDFExporter());
    }

    @Autowired
    public QrCodeAndPdfService(QRCodeGenerator qrCodeGenerator, PDFExporter pdfExporter) {
        this.qrCodeGenerator = qrCodeGenerator;
        this.pdfExporter = pdfExporter;
    }

    /**
     * Sinh mã SVG QR 21x21 chuẩn có 3 góc định vị dựa trên dữ liệu vé.
     */
    public String generateQrSvg(String payload) {
        return qrCodeGenerator.generateSvg(payload);
    }

    /**
     * Xuất vé điện tử sang định dạng PDF 1.4 nhị phân hợp lệ để tải xuống trực tiếp.
     */
    public byte[] generateTicketPdf(Ticket ticket) {
        return pdfExporter.exportTicketToPdf(ticket);
    }
}
