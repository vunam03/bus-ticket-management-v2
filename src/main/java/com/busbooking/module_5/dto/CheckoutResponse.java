package com.busbooking.module_5.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO phản hồi kết quả phiên Checkout hoặc kết quả thanh toán đặt vé thành công (Module 5).
 */
public class CheckoutResponse {

    private String holdCode;
    private String tripCode;
    private String routeName;
    private String departureTimeText;
    private List<String> seatNumbers = new ArrayList<>();
    private BigDecimal totalAmount;
    private String formattedTotal;
    private LocalDateTime holdExpiresAt;
    private boolean paymentSuccess;
    private String paymentMethodName;
    private String message;
    private List<Long> ticketIds = new ArrayList<>();
    private List<String> ticketCodes = new ArrayList<>();

    public String getHoldCode() {
        return holdCode;
    }

    public void setHoldCode(String holdCode) {
        this.holdCode = holdCode;
    }

    public String getTripCode() {
        return tripCode;
    }

    public void setTripCode(String tripCode) {
        this.tripCode = tripCode;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getDepartureTimeText() {
        return departureTimeText;
    }

    public void setDepartureTimeText(String departureTimeText) {
        this.departureTimeText = departureTimeText;
    }

    public List<String> getSeatNumbers() {
        return seatNumbers;
    }

    public void setSeatNumbers(List<String> seatNumbers) {
        this.seatNumbers = seatNumbers;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getFormattedTotal() {
        return formattedTotal;
    }

    public void setFormattedTotal(String formattedTotal) {
        this.formattedTotal = formattedTotal;
    }

    public LocalDateTime getHoldExpiresAt() {
        return holdExpiresAt;
    }

    public void setHoldExpiresAt(LocalDateTime holdExpiresAt) {
        this.holdExpiresAt = holdExpiresAt;
    }

    public boolean isPaymentSuccess() {
        return paymentSuccess;
    }

    public void setPaymentSuccess(boolean paymentSuccess) {
        this.paymentSuccess = paymentSuccess;
    }

    public String getPaymentMethodName() {
        return paymentMethodName;
    }

    public void setPaymentMethodName(String paymentMethodName) {
        this.paymentMethodName = paymentMethodName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Long> getTicketIds() {
        return ticketIds;
    }

    public void setTicketIds(List<Long> ticketIds) {
        this.ticketIds = ticketIds;
    }

    public List<String> getTicketCodes() {
        return ticketCodes;
    }

    public void setTicketCodes(List<String> ticketCodes) {
        this.ticketCodes = ticketCodes;
    }
}
