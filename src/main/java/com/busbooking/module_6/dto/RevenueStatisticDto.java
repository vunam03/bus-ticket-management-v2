package com.busbooking.module_6.dto;

import com.busbooking.common.utils.CurrencyUtils;
import java.math.BigDecimal;

public class RevenueStatisticDto {

    private String periodLabel;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private long ticketCount;
    private BigDecimal averageTicketPrice = BigDecimal.ZERO;
    private double sharePercentage;

    public RevenueStatisticDto() {
    }

    public RevenueStatisticDto(String periodLabel, BigDecimal totalRevenue, long ticketCount,
                               BigDecimal averageTicketPrice, double sharePercentage) {
        this.periodLabel = periodLabel;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.ticketCount = ticketCount;
        this.averageTicketPrice = averageTicketPrice != null ? averageTicketPrice : BigDecimal.ZERO;
        this.sharePercentage = sharePercentage;
    }

    public String getFormattedRevenue() {
        return CurrencyUtils.formatVnd(totalRevenue);
    }

    public String getFormattedAveragePrice() {
        return CurrencyUtils.formatVnd(averageTicketPrice);
    }

    public String getPeriodLabel() {
        return periodLabel;
    }

    public void setPeriodLabel(String periodLabel) {
        this.periodLabel = periodLabel;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getTicketCount() {
        return ticketCount;
    }

    public void setTicketCount(long ticketCount) {
        this.ticketCount = ticketCount;
    }

    public BigDecimal getAverageTicketPrice() {
        return averageTicketPrice;
    }

    public void setAverageTicketPrice(BigDecimal averageTicketPrice) {
        this.averageTicketPrice = averageTicketPrice;
    }

    public double getSharePercentage() {
        return sharePercentage;
    }

    public void setSharePercentage(double sharePercentage) {
        this.sharePercentage = sharePercentage;
    }

    public String getLabel() {
        return periodLabel;
    }

    public long getTripCount() {
        return ticketCount > 0 ? 1L : 0L;
    }

    public long getCancelledCount() {
        return 0L;
    }
}
