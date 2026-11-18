package com.busbooking.module_3.dto;

import java.math.BigDecimal;

/**
 * DTO đề xuất tuyến đường nổi bật và thống kê giá vé thấp nhất theo tuyến (Module 3).
 */
public class RouteRecommendationDto {

    private String routeName;
    private String departureProvince;
    private String arrivalProvince;
    private int activeTripCount;
    private int totalAvailableSeats;
    private BigDecimal minPrice;
    private String formattedMinPrice;
    private String estimatedDuration;
    private String imageUrl;
    private String highlight;

    public RouteRecommendationDto() {
    }

    public RouteRecommendationDto(String routeName,
                                  String departureProvince,
                                  String arrivalProvince,
                                  int activeTripCount,
                                  int totalAvailableSeats,
                                  BigDecimal minPrice,
                                  String formattedMinPrice,
                                  String estimatedDuration,
                                  String imageUrl,
                                  String highlight) {
        this.routeName = routeName;
        this.departureProvince = departureProvince;
        this.arrivalProvince = arrivalProvince;
        this.activeTripCount = activeTripCount;
        this.totalAvailableSeats = totalAvailableSeats;
        this.minPrice = minPrice;
        this.formattedMinPrice = formattedMinPrice;
        this.estimatedDuration = estimatedDuration;
        this.imageUrl = imageUrl;
        this.highlight = highlight;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
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

    public int getActiveTripCount() {
        return activeTripCount;
    }

    public void setActiveTripCount(int activeTripCount) {
        this.activeTripCount = activeTripCount;
    }

    public int getTotalAvailableSeats() {
        return totalAvailableSeats;
    }

    public void setTotalAvailableSeats(int totalAvailableSeats) {
        this.totalAvailableSeats = totalAvailableSeats;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public String getFormattedMinPrice() {
        return formattedMinPrice;
    }

    public void setFormattedMinPrice(String formattedMinPrice) {
        this.formattedMinPrice = formattedMinPrice;
    }

    public String getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(String estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getHighlight() {
        return highlight;
    }

    public void setHighlight(String highlight) {
        this.highlight = highlight;
    }
}
