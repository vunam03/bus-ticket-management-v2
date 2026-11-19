package com.busbooking.module_6.dto;

import com.busbooking.common.utils.CurrencyUtils;
import java.math.BigDecimal;

public class MonthlyReportDto {

    private int month;
    private int year;
    private String monthLabel;
    private BigDecimal revenue = BigDecimal.ZERO;
    private long paidTickets;
    private long cancelledTickets;
    private double growthRate;

    public MonthlyReportDto() {
    }

    public MonthlyReportDto(int month, int year, String monthLabel, BigDecimal revenue,
                            long paidTickets, long cancelledTickets, double growthRate) {
        this.month = month;
        this.year = year;
        this.monthLabel = monthLabel;
        this.revenue = revenue != null ? revenue : BigDecimal.ZERO;
        this.paidTickets = paidTickets;
        this.cancelledTickets = cancelledTickets;
        this.growthRate = growthRate;
    }

    public String getFormattedRevenue() {
        return CurrencyUtils.formatVnd(revenue);
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getMonthLabel() {
        return monthLabel;
    }

    public void setMonthLabel(String monthLabel) {
        this.monthLabel = monthLabel;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public long getPaidTickets() {
        return paidTickets;
    }

    public void setPaidTickets(long paidTickets) {
        this.paidTickets = paidTickets;
    }

    public long getCancelledTickets() {
        return cancelledTickets;
    }

    public void setCancelledTickets(long cancelledTickets) {
        this.cancelledTickets = cancelledTickets;
    }

    public double getGrowthRate() {
        return growthRate;
    }

    public void setGrowthRate(double growthRate) {
        this.growthRate = growthRate;
    }

    public long getTotalTrips() {
        return paidTickets;
    }

    public double getGrowthRatePercent() {
        return growthRate;
    }
}
