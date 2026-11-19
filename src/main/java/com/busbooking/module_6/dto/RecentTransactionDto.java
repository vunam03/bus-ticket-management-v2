package com.busbooking.module_6.dto;

import com.busbooking.common.utils.CurrencyUtils;
import java.math.BigDecimal;

public class RecentTransactionDto {

    private String transactionCode;
    private String ticketCode;
    private String passengerName;
    private String passengerPhone;
    private String routeName;
    private String companyName;
    private BigDecimal amount = BigDecimal.ZERO;
    private String paymentMethodLabel;
    private String status;
    private String formattedTime;

    public RecentTransactionDto() {
    }

    public RecentTransactionDto(String transactionCode, String ticketCode, String passengerName,
                                String passengerPhone, String routeName, String companyName,
                                BigDecimal amount, String paymentMethodLabel, String status, String formattedTime) {
        this.transactionCode = transactionCode;
        this.ticketCode = ticketCode;
        this.passengerName = passengerName;
        this.passengerPhone = passengerPhone;
        this.routeName = routeName;
        this.companyName = companyName;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.paymentMethodLabel = paymentMethodLabel;
        this.status = status;
        this.formattedTime = formattedTime;
    }

    public String getFormattedAmount() {
        return CurrencyUtils.formatVnd(amount);
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getPassengerPhone() {
        return passengerPhone;
    }

    public void setPassengerPhone(String passengerPhone) {
        this.passengerPhone = passengerPhone;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentMethodLabel() {
        return paymentMethodLabel;
    }

    public void setPaymentMethodLabel(String paymentMethodLabel) {
        this.paymentMethodLabel = paymentMethodLabel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFormattedTime() {
        return formattedTime;
    }

    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }

    private Long ticketId;
    private String seatNumber = "-";
    private String ticketStatus;
    private String paymentStatus;
    private java.time.LocalDateTime bookingTime;

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getCustomerName() {
        return passengerName;
    }

    public String getCustomerPhone() {
        return passengerPhone;
    }

    public String getSeatNumber() {
        return seatNumber != null ? seatNumber : "-";
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getTicketStatus() {
        return ticketStatus != null ? ticketStatus : status;
    }

    public void setTicketStatus(String ticketStatus) {
        this.ticketStatus = ticketStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus != null ? paymentStatus : status;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public java.time.LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(java.time.LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }
}
