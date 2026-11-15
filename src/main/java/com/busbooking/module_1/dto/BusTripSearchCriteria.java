package com.busbooking.module_1.dto;

import com.busbooking.common.enums.BusType;
import com.busbooking.common.enums.TripStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Tiêu chí lọc, tìm kiếm & phân trang danh sách chuyến xe khu vực Quản trị (Module 1).
 */
public class BusTripSearchCriteria {

    private String keyword;
    private Long companyId;
    private TripStatus status;
    private BusType busType;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate departureDate;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sortBy = "DEPARTURE_DESC";
    private int page = 0;
    private int size = 10;

    public boolean hasAnyFilter() {
        return (keyword != null && !keyword.isBlank())
                || companyId != null
                || status != null
                || busType != null
                || departureDate != null
                || minPrice != null
                || maxPrice != null;
    }

    public String getNormalizedKeyword() {
        return keyword == null ? "" : keyword.trim().toLowerCase();
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public TripStatus getStatus() {
        return status;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }

    public BusType getBusType() {
        return busType;
    }

    public void setBusType(BusType busType) {
        this.busType = busType;
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

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public int getPage() {
        return Math.max(0, page);
    }

    public void setPage(int page) {
        this.page = Math.max(0, page);
    }

    public int getSize() {
        return size <= 0 ? 10 : Math.min(size, 50);
    }

    public void setSize(int size) {
        this.size = size <= 0 ? 10 : Math.min(size, 50);
    }
}
