package com.busbooking.module_5.service;

import com.busbooking.common.enums.PaymentMethod;
import com.busbooking.common.enums.PaymentStatus;
import com.busbooking.common.enums.TicketStatus;
import com.busbooking.common.utils.CurrencyUtils;
import com.busbooking.module_5.dto.PaymentGatewayDto;
import com.busbooking.module_5.model.PaymentTransaction;
import com.busbooking.module_5.model.Ticket;
import com.busbooking.module_5.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dịch vụ quản lý thông tin cổng thanh toán, lịch sử giao dịch và tính toán chính sách hoàn tiền (Module 5).
 */
@Service
public class PaymentService {

    private final PaymentTransactionRepository transactionRepository;

    public PaymentService(PaymentTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Trả về danh sách cổng thanh toán được hệ thống hỗ trợ kèm hướng dẫn đối soát.
     */
    public List<PaymentGatewayDto> getSupportedGateways() {
        return List.of(
                getGatewayInfo(PaymentMethod.BANK_TRANSFER),
                getGatewayInfo(PaymentMethod.E_WALLET),
                getGatewayInfo(PaymentMethod.CASH)
        );
    }

    /**
     * Trả về thông tin chi tiết cho từng phương thức thanh toán.
     */
    public PaymentGatewayDto getGatewayInfo(PaymentMethod method) {
        PaymentMethod active = method != null ? method : PaymentMethod.BANK_TRANSFER;
        return switch (active) {
            case BANK_TRANSFER -> new PaymentGatewayDto(
                    PaymentMethod.BANK_TRANSFER,
                    "Chuyển khoản nhanh qua mã VietQR 24/7, hệ thống tự động xác nhận và cấp mã QR lên xe ngay lập tức.",
                    true,
                    "bg-primary"
            );
            case E_WALLET -> new PaymentGatewayDto(
                    PaymentMethod.E_WALLET,
                    "Thanh toán qua ví điện tử MoMo, VNPay hoặc ZaloPay với tốc độ xử lý dưới 2 giây.",
                    true,
                    "bg-info text-dark"
            );
            case CASH -> new PaymentGatewayDto(
                    PaymentMethod.CASH,
                    "Giữ chỗ trực tuyến và thanh toán tiền mặt trực tiếp tại quầy vé hoặc cho phụ xe trước khi khởi hành 30 phút.",
                    false,
                    "bg-warning text-dark"
            );
        };
    }

    /**
     * Lấy lịch sử giao dịch thanh toán và hoàn tiền gắn với một vé cụ thể.
     */
    @Transactional(readOnly = true)
    public List<PaymentTransaction> getTransactionsByTicketId(Long ticketId) {
        return transactionRepository.findByTicketIdOrderByCreatedAtDesc(ticketId);
    }

    /**
     * Tính toán trước chính sách hủy vé và số tiền hoàn dự kiến dựa trên thời gian trước giờ xe chạy.
     */
    public Map<String, Object> calculateRefundPreview(Ticket ticket, boolean isAdmin) {
        Map<String, Object> preview = new LinkedHashMap<>();
        if (ticket == null) {
            preview.put("cancellable", false);
            preview.put("refundRatePercent", 0);
            preview.put("refundAmount", BigDecimal.ZERO);
            preview.put("formattedRefundAmount", CurrencyUtils.formatVnd(BigDecimal.ZERO));
            preview.put("policyMessage", "Không có dữ liệu vé để tính chính sách hoàn hủy.");
            return preview;
        }

        if (ticket.getTicketStatus() == TicketStatus.CANCELLED) {
            preview.put("cancellable", false);
            preview.put("refundRatePercent", 0);
            preview.put("refundAmount", ticket.getRefundAmount());
            preview.put("formattedRefundAmount", CurrencyUtils.formatVnd(ticket.getRefundAmount()));
            preview.put("policyMessage", "Vé này đã được hủy trước đó.");
            return preview;
        }

        long hoursRemaining = Duration.between(LocalDateTime.now(), ticket.getBusTrip().getDepartureTime()).toHours();
        boolean cancellable = isAdmin || hoursRemaining >= 4;
        int refundPercent = 0;
        BigDecimal refundAmount = BigDecimal.ZERO;

        if (cancellable && ticket.getPaymentStatus() == PaymentStatus.PAID) {
            if (isAdmin || hoursRemaining >= 24) {
                refundPercent = 90;
            } else {
                refundPercent = 70;
            }
            BigDecimal rate = BigDecimal.valueOf(refundPercent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            refundAmount = ticket.getTotalAmount().multiply(rate).setScale(0, RoundingMode.HALF_UP);
        }

        String policyMessage;
        if (!cancellable) {
            policyMessage = "Quá hạn hủy vé trực tuyến (chỉ hỗ trợ hủy trước giờ khởi hành tối thiểu 4 giờ).";
        } else if (ticket.getPaymentStatus() != PaymentStatus.PAID) {
            policyMessage = "Vé chưa thanh toán trực tuyến nên sẽ được hủy chỗ mà không phát sinh hoàn tiền.";
        } else {
            policyMessage = "Đủ điều kiện hủy vé trực tuyến: hoàn " + refundPercent + "% tổng tiền vé.";
        }

        preview.put("ticketCode", ticket.getTicketCode());
        preview.put("hoursBeforeDeparture", Math.max(0L, hoursRemaining));
        preview.put("cancellable", cancellable);
        preview.put("refundRatePercent", refundPercent);
        preview.put("refundAmount", refundAmount);
        preview.put("formattedRefundAmount", CurrencyUtils.formatVnd(refundAmount));
        preview.put("policyMessage", policyMessage);
        return preview;
    }
}
