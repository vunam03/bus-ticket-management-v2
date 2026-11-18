package com.busbooking.module_5.dto;

import com.busbooking.common.enums.PaymentMethod;

/**
 * DTO mô tả cổng thanh toán được hỗ trợ và hướng dẫn đối soát giao dịch (Module 5).
 */
public class PaymentGatewayDto {

    private PaymentMethod method;
    private String code;
    private String displayName;
    private String instruction;
    private boolean instantConfirmation;
    private String badgeClass;

    public PaymentGatewayDto() {
    }

    public PaymentGatewayDto(PaymentMethod method,
                             String instruction,
                             boolean instantConfirmation,
                             String badgeClass) {
        this.method = method;
        this.code = method.name();
        this.displayName = method.getDisplayName();
        this.instruction = instruction;
        this.instantConfirmation = instantConfirmation;
        this.badgeClass = badgeClass;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public boolean isInstantConfirmation() {
        return instantConfirmation;
    }

    public void setInstantConfirmation(boolean instantConfirmation) {
        this.instantConfirmation = instantConfirmation;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public void setBadgeClass(String badgeClass) {
        this.badgeClass = badgeClass;
    }
}
