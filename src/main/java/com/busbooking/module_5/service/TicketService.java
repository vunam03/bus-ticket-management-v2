package com.busbooking.module_5.service;

import com.busbooking.common.enums.TicketStatus;
import com.busbooking.common.exception.BusinessException;
import com.busbooking.common.exception.ResourceNotFoundException;
import com.busbooking.module_2.model.User;
import com.busbooking.module_2.service.AuthAccountService;
import com.busbooking.module_5.dto.TicketDetailDto;
import com.busbooking.module_5.model.Ticket;
import com.busbooking.module_5.repository.TicketRepository;
import com.busbooking.module_5.utils.QRCodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Dịch vụ tra cứu vé theo mã, lọc danh sách vé theo trạng thái và dựng DTO thẻ lên xe (Module 5).
 */
@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final AuthAccountService authAccountService;
    private final PaymentService paymentService;
    private final QRCodeGenerator qrCodeGenerator;

    public TicketService(TicketRepository ticketRepository,
                         AuthAccountService authAccountService,
                         PaymentService paymentService,
                         QRCodeGenerator qrCodeGenerator) {
        this.ticketRepository = ticketRepository;
        this.authAccountService = authAccountService;
        this.paymentService = paymentService;
        this.qrCodeGenerator = qrCodeGenerator;
    }

    /**
     * Lọc vé của người dùng hiện tại theo trạng thái (nếu status == null thì lấy tất cả).
     */
    @Transactional(readOnly = true)
    public List<Ticket> findUserTicketsByStatus(String username, TicketStatus status) {
        User user = authAccountService.getByUsername(username);
        if (status == null) {
            return ticketRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        }
        return ticketRepository.findByUserIdAndTicketStatusOrderByCreatedAtDesc(user.getId(), status);
    }

    /**
     * Tra cứu vé theo mã vé (ticketCode) có kiểm tra quyền sở hữu.
     */
    @Transactional(readOnly = true)
    public Ticket findTicketByCodeForUser(String ticketCode, String username, boolean isAdmin) {
        if (ticketCode == null || ticketCode.isBlank()) {
            throw new ResourceNotFoundException("Vui lòng cung cấp mã vé cần tra cứu.");
        }
        Ticket ticket = ticketRepository.findByTicketCodeIgnoreCase(ticketCode.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé có mã: " + ticketCode));
        if (!isAdmin) {
            User user = authAccountService.getByUsername(username);
            if (!ticket.getUser().getId().equals(user.getId())) {
                throw new BusinessException("ACCESS_DENIED_TICKET", "Bạn không có quyền truy cập vé của hành khách khác.");
            }
        }
        return ticket;
    }

    /**
     * Tìm nhiều vé theo danh sách mã vé (phục vụ màn hình xác nhận đặt nhiều ghế liên tiếp).
     */
    @Transactional(readOnly = true)
    public List<Ticket> findTicketsByCodesForUser(List<String> ticketCodes, String username, boolean isAdmin) {
        List<Ticket> result = new ArrayList<>();
        if (ticketCodes == null) {
            return result;
        }
        for (String code : ticketCodes) {
            if (code != null && !code.isBlank()) {
                result.add(findTicketByCodeForUser(code, username, isAdmin));
            }
        }
        return result;
    }

    /**
     * Chuyển đổi Entity Ticket sang TicketDetailDto kèm QR SVG và thông tin hoàn tiền dự kiến.
     */
    @Transactional(readOnly = true)
    public TicketDetailDto buildTicketDetailDto(Long ticketId, String username, boolean isAdmin) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé với ID: " + ticketId));
        if (!isAdmin) {
            User user = authAccountService.getByUsername(username);
            if (!ticket.getUser().getId().equals(user.getId())) {
                throw new BusinessException("ACCESS_DENIED_TICKET", "Bạn không có quyền xem chi tiết vé này.");
            }
        }
        Map<String, Object> refundPreview = paymentService.calculateRefundPreview(ticket, isAdmin);
        boolean cancellable = Boolean.TRUE.equals(refundPreview.get("cancellable"));
        String policyHint = String.valueOf(refundPreview.get("policyMessage"));
        String qrSvg = qrCodeGenerator.generateSvg(ticket.getQrCodePayload());
        return TicketDetailDto.fromEntity(ticket, qrSvg, cancellable, policyHint);
    }
}
