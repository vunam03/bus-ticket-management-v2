package com.busbooking.module_3.dto;

import com.busbooking.common.enums.BusType;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TripSearchFilterDto {

    private String departureProvince;
    private String arrivalProvince;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate departureDate;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BusType busType;
    private Long companyId;
    private String timeSlot; // MORNING (05-12), AFTERNOON (12-18), EVENING (18-24), NIGHT (00-05)
    private String sortBy = "TIME_ASC"; // TIME_ASC, PRICE_ASC, PRICE_DESC, SEATS_DESC

    public String getDepartureProvince() {
        return departureProvince;
    }

    public void setDepartureProvince(String departureProvince) {
        this.departureProvince = departureProvince;
    }

    public String getArrivalProvince() {
        return arrivalProvince;
    }

    public void setArrivalProvince(String arrivalProvince) {
        this.arrivalProvince = arrivalProvince;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public BusType getBusType() {
        return busType;
    }

    public void setBusType(BusType busType) {
        this.busType = busType;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
}
