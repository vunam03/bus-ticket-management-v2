package com.busbooking.module_5.dto;

import com.busbooking.common.utils.CurrencyUtils;
import com.busbooking.common.utils.DateTimeUtils;
import com.busbooking.module_5.model.Ticket;

import java.math.BigDecimal;

/**
 * DTO chi tiết thẻ lên xe điện tử kèm chính sách hoàn hủy và chuỗi QR SVG (Module 5).
 */
public class TicketDetailDto {

    private Long id;
    private String ticketCode;
    private String companyName;
    private String tripCode;
    private String routeName;
    private String departureStation;
    private String arrivalStation;
    private String departureTimeText;
    private String seatNumber;
    private Integer floorNumber;
    private String passengerName;
    private String passengerPhone;
    private String passengerEmail;
    private String pickupNote;
    private BigDecimal totalAmount;
    private String formattedTotalAmount;
    private BigDecimal refundAmount;
    private String formattedRefundAmount;
    private String ticketStatusCode;
    private String ticketStatusLabel;
    private String paymentStatusCode;
    private String paymentStatusLabel;
    private String paymentMethodLabel;
    private String qrCodePayload;
    private String qrSvg;
    private boolean cancellableOnline;
    private String refundPolicyHint;
    private String createdAtText;

    public static TicketDetailDto fromEntity(Ticket ticket, String qrSvg, boolean cancellable, String refundHint) {
        TicketDetailDto dto = new TicketDetailDto();
        dto.id = ticket.getId();
        dto.ticketCode = ticket.getTicketCode();
        if (ticket.getBusTrip() != null) {
            dto.tripCode = ticket.getBusTrip().getTripCode();
            dto.routeName = ticket.getBusTrip().getDepartureProvince() + " → " + ticket.getBusTrip().getArrivalProvince();
            dto.departureStation = ticket.getBusTrip().getDepartureStation();
            dto.arrivalStation = ticket.getBusTrip().getArrivalStation();
            dto.departureTimeText = DateTimeUtils.formatDateTime(ticket.getBusTrip().getDepartureTime());
            if (ticket.getBusTrip().getBusCompany() != null) {
                dto.companyName = ticket.getBusTrip().getBusCompany().getName();
            }
        }
        if (ticket.getSeat() != null) {
            dto.seatNumber = ticket.getSeat().getSeatNumber();
            dto.floorNumber = ticket.getSeat().getFloorNumber();
        }
        dto.passengerName = ticket.getPassengerName();
        dto.passengerPhone = ticket.getPassengerPhone();
        dto.passengerEmail = ticket.getPassengerEmail();
        dto.pickupNote = ticket.getPickupNote();
        dto.totalAmount = ticket.getTotalAmount();
        dto.formattedTotalAmount = CurrencyUtils.formatVnd(ticket.getTotalAmount());
        dto.refundAmount = ticket.getRefundAmount();
        dto.formattedRefundAmount = CurrencyUtils.formatVnd(ticket.getRefundAmount());
        dto.ticketStatusCode = ticket.getTicketStatus().name();
        dto.ticketStatusLabel = ticket.getTicketStatus().getDisplayName();
        dto.paymentStatusCode = ticket.getPaymentStatus().name();
        dto.paymentStatusLabel = ticket.getPaymentStatus().getDisplayName();
        dto.paymentMethodLabel = ticket.getPaymentMethod().getDisplayName();
        dto.qrCodePayload = ticket.getQrCodePayload();
        dto.qrSvg = qrSvg;
        dto.cancellableOnline = cancellable;
        dto.refundPolicyHint = refundHint;
        dto.createdAtText = DateTimeUtils.formatDateTime(ticket.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getTripCode() {
        return tripCode;
    }

    public String getRouteName() {
        return routeName;
    }

    public String getDepartureStation() {
        return departureStation;
    }

    public String getArrivalStation() {
        return arrivalStation;
    }

    public String getDepartureTimeText() {
        return departureTimeText;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public Integer getFloorNumber() {
        return floorNumber;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public String getPassengerPhone() {
        return passengerPhone;
    }

    public String getPassengerEmail() {
        return passengerEmail;
    }

    public String getPickupNote() {
        return pickupNote;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getFormattedTotalAmount() {
        return formattedTotalAmount;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public String getFormattedRefundAmount() {
        return formattedRefundAmount;
    }

    public String getTicketStatusCode() {
        return ticketStatusCode;
    }

    public String getTicketStatusLabel() {
        return ticketStatusLabel;
    }

    public String getPaymentStatusCode() {
        return paymentStatusCode;
    }

    public String getPaymentStatusLabel() {
        return paymentStatusLabel;
    }

    public String getPaymentMethodLabel() {
        return paymentMethodLabel;
    }

    public String getQrCodePayload() {
        return qrCodePayload;
    }

    public String getQrSvg() {
        return qrSvg;
    }

    public boolean isCancellableOnline() {
        return cancellableOnline;
    }

    public String getRefundPolicyHint() {
        return refundPolicyHint;
    }

    public String getCreatedAtText() {
        return createdAtText;
    }
}
