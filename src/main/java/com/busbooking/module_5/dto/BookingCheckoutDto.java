package com.busbooking.module_5.dto;

import com.busbooking.common.enums.PaymentMethod;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BookingCheckoutDto {

    @NotBlank(message = "Mã giữ ghế không hợp lệ")
    private String holdCode;

    @NotBlank(message = "Vui lòng nhập họ tên hành khách")
    private String passengerName;

    @NotBlank(message = "Vui lòng nhập số điện thoại liên hệ")
    private String passengerPhone;

    @Email(message = "Email không đúng định dạng")
    private String passengerEmail;

    private String pickupNote;

    @NotNull(message = "Vui lòng chọn phương thức thanh toán")
    private PaymentMethod paymentMethod = PaymentMethod.BANK_TRANSFER;

    private boolean simulatePaymentFailure = false;

    public String getHoldCode() {
        return holdCode;
    }

    public void setHoldCode(String holdCode) {
        this.holdCode = holdCode;
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

    public String getPassengerEmail() {
        return passengerEmail;
    }

    public void setPassengerEmail(String passengerEmail) {
        this.passengerEmail = passengerEmail;
    }

    public String getPickupNote() {
        return pickupNote;
    }

    public void setPickupNote(String pickupNote) {
        this.pickupNote = pickupNote;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public boolean isSimulatePaymentFailure() {
        return simulatePaymentFailure;
    }

    public void setSimulatePaymentFailure(boolean simulatePaymentFailure) {
        this.simulatePaymentFailure = simulatePaymentFailure;
    }

    public boolean isSimulateFailure() {
        return simulatePaymentFailure;
    }

    public void setSimulateFailure(boolean simulateFailure) {
        this.simulatePaymentFailure = simulateFailure;
    }
}
