package com.busbooking.module_3.dto;

import com.busbooking.common.enums.BusType;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO nhận yêu cầu tìm kiếm và lọc chuyến xe trên giao diện & API (Module 3).
 */
public class SearchFilterRequest {

    private String departureProvince;
    private String arrivalProvince;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate departureDate;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BusType busType;
    private Long companyId;
    private String timeSlot;
    private String sortBy = "TIME_ASC";

    public TripSearchFilterDto toFilterDto() {
        TripSearchFilterDto dto = new TripSearchFilterDto();
        dto.setDepartureProvince(departureProvince);
        dto.setArrivalProvince(arrivalProvince);
        dto.setDepartureDate(departureDate);
        dto.setMinPrice(minPrice);
        dto.setMaxPrice(maxPrice);
        dto.setBusType(busType);
        dto.setCompanyId(companyId);
        dto.setTimeSlot(timeSlot);
        dto.setSortBy(sortBy != null && !sortBy.isBlank() ? sortBy : "TIME_ASC");
        return dto;
    }

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
