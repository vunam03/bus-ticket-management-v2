package com.busbooking.module_6.dto;

import com.busbooking.common.utils.CurrencyUtils;
import java.math.BigDecimal;

public class TicketSalesStatDto {

    private String dimensionName;
    private String secondaryInfo;
    private long ticketsSold;
    private BigDecimal revenue = BigDecimal.ZERO;
    private double percentageOfTotal;

    public TicketSalesStatDto() {
    }

    public TicketSalesStatDto(String dimensionName, String secondaryInfo, long ticketsSold,
                              BigDecimal revenue, double percentageOfTotal) {
        this.dimensionName = dimensionName;
        this.secondaryInfo = secondaryInfo;
        this.ticketsSold = ticketsSold;
        this.revenue = revenue != null ? revenue : BigDecimal.ZERO;
        this.percentageOfTotal = percentageOfTotal;
    }

    public String getFormattedRevenue() {
        return CurrencyUtils.formatVnd(revenue);
    }

    public String getDimensionName() {
        return dimensionName;
    }

    public void setDimensionName(String dimensionName) {
        this.dimensionName = dimensionName;
    }

    public String getSecondaryInfo() {
        return secondaryInfo;
    }

    public void setSecondaryInfo(String secondaryInfo) {
        this.secondaryInfo = secondaryInfo;
    }

    public long getTicketsSold() {
        return ticketsSold;
    }

    public void setTicketsSold(long ticketsSold) {
        this.ticketsSold = ticketsSold;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public double getPercentageOfTotal() {
        return percentageOfTotal;
    }

    public void setPercentageOfTotal(double percentageOfTotal) {
        this.percentageOfTotal = percentageOfTotal;
    }

    public String getCategoryName() {
        return dimensionName;
    }

    public double getSharePercentage() {
        return percentageOfTotal;
    }
}
