package com.busbooking.module_6.dto;

import com.busbooking.common.utils.CurrencyUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DashboardSummaryDto {

    private int selectedYear;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private long totalTrips;
    private long totalPaidTickets;
    private long totalCustomers;

    private long paidTicketsCount;
    private long unpaidTicketsCount;
    private long cancelledTicketsCount;
    private long refundedTicketsCount;

    private long totalSeatsCapacity;
    private long bookedSeatsCount;
    private long heldSeatsCount;
    private long availableSeatsCount;
    private double overallOccupancyRate;

    private List<String> monthlyLabels = new ArrayList<>();
    private List<BigDecimal> monthlyRevenues = new ArrayList<>();
    private List<String> dailyLabels = new ArrayList<>();
    private List<BigDecimal> dailyRevenues = new ArrayList<>();

    private List<MonthlyReportDto> monthlyBreakdown = new ArrayList<>();
    private List<TicketSalesStatDto> companySalesStats = new ArrayList<>();
    private List<TicketSalesStatDto> routeSalesStats = new ArrayList<>();
    private List<SeatOccupancyDto> tripOccupancies = new ArrayList<>();
    private List<RecentTransactionDto> recentTransactions = new ArrayList<>();

    public String getFormattedTotalRevenue() {
        return CurrencyUtils.formatVnd(totalRevenue);
    }

    public int getSelectedYear() {
        return selectedYear;
    }

    public void setSelectedYear(int selectedYear) {
        this.selectedYear = selectedYear;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getTotalTrips() {
        return totalTrips;
    }

    public void setTotalTrips(long totalTrips) {
        this.totalTrips = totalTrips;
    }

    public long getTotalPaidTickets() {
        return totalPaidTickets;
    }

    public void setTotalPaidTickets(long totalPaidTickets) {
        this.totalPaidTickets = totalPaidTickets;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public long getPaidTicketsCount() {
        return paidTicketsCount;
    }

    public void setPaidTicketsCount(long paidTicketsCount) {
        this.paidTicketsCount = paidTicketsCount;
    }

    public long getUnpaidTicketsCount() {
        return unpaidTicketsCount;
    }

    public void setUnpaidTicketsCount(long unpaidTicketsCount) {
        this.unpaidTicketsCount = unpaidTicketsCount;
    }

    public long getCancelledTicketsCount() {
        return cancelledTicketsCount;
    }

    public void setCancelledTicketsCount(long cancelledTicketsCount) {
        this.cancelledTicketsCount = cancelledTicketsCount;
    }

    public long getRefundedTicketsCount() {
        return refundedTicketsCount;
    }

    public void setRefundedTicketsCount(long refundedTicketsCount) {
        this.refundedTicketsCount = refundedTicketsCount;
    }

    public long getTotalSeatsCapacity() {
        return totalSeatsCapacity;
    }

    public void setTotalSeatsCapacity(long totalSeatsCapacity) {
        this.totalSeatsCapacity = totalSeatsCapacity;
    }

    public long getBookedSeatsCount() {
        return bookedSeatsCount;
    }

    public void setBookedSeatsCount(long bookedSeatsCount) {
        this.bookedSeatsCount = bookedSeatsCount;
    }

    public long getHeldSeatsCount() {
        return heldSeatsCount;
    }

    public void setHeldSeatsCount(long heldSeatsCount) {
        this.heldSeatsCount = heldSeatsCount;
    }

    public long getAvailableSeatsCount() {
        return availableSeatsCount;
    }

    public void setAvailableSeatsCount(long availableSeatsCount) {
        this.availableSeatsCount = availableSeatsCount;
    }

    public double getOverallOccupancyRate() {
        return overallOccupancyRate;
    }

    public void setOverallOccupancyRate(double overallOccupancyRate) {
        this.overallOccupancyRate = overallOccupancyRate;
    }

    public List<String> getMonthlyLabels() {
        return monthlyLabels;
    }

    public void setMonthlyLabels(List<String> monthlyLabels) {
        this.monthlyLabels = monthlyLabels;
    }

    public List<BigDecimal> getMonthlyRevenues() {
        return monthlyRevenues;
    }

    public void setMonthlyRevenues(List<BigDecimal> monthlyRevenues) {
        this.monthlyRevenues = monthlyRevenues;
    }

    public List<String> getDailyLabels() {
        return dailyLabels;
    }

    public void setDailyLabels(List<String> dailyLabels) {
        this.dailyLabels = dailyLabels;
    }

    public List<BigDecimal> getDailyRevenues() {
        return dailyRevenues;
    }

    public void setDailyRevenues(List<BigDecimal> dailyRevenues) {
        this.dailyRevenues = dailyRevenues;
    }

    public List<MonthlyReportDto> getMonthlyBreakdown() {
        return monthlyBreakdown;
    }

    public void setMonthlyBreakdown(List<MonthlyReportDto> monthlyBreakdown) {
        this.monthlyBreakdown = monthlyBreakdown;
    }

    public List<TicketSalesStatDto> getCompanySalesStats() {
        return companySalesStats;
    }

    public void setCompanySalesStats(List<TicketSalesStatDto> companySalesStats) {
        this.companySalesStats = companySalesStats;
    }

    public List<TicketSalesStatDto> getRouteSalesStats() {
        return routeSalesStats;
    }

    public void setRouteSalesStats(List<TicketSalesStatDto> routeSalesStats) {
        this.routeSalesStats = routeSalesStats;
    }

    public List<SeatOccupancyDto> getTripOccupancies() {
        return tripOccupancies;
    }

    public void setTripOccupancies(List<SeatOccupancyDto> tripOccupancies) {
        this.tripOccupancies = tripOccupancies;
    }

    public List<RecentTransactionDto> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(List<RecentTransactionDto> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }
}
