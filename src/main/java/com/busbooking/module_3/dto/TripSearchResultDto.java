package com.busbooking.module_3.dto;

import com.busbooking.module_1.model.BusTrip;
import com.busbooking.module_3.utils.CurrencyFormatter;
import com.busbooking.module_3.utils.DateUtils;

import java.math.BigDecimal;

/**
 * DTO hiển thị kết quả tìm kiếm chuyến xe kèm thời lượng, khung giờ và hình ảnh điểm đến (Module 3).
 */
public class TripSearchResultDto {

    private Long tripId;
    private String tripCode;
    private String companyName;
    private Double companyRating;
    private String busTypeName;
    private String departureProvince;
    private String departureStation;
    private String arrivalProvince;
    private String arrivalStation;
    private String departureTimeText;
    private String arrivalTimeText;
    private String durationText;
    private String timeSlotLabel;
    private BigDecimal ticketPrice;
    private String formattedPrice;
    private String priceTier;
    private Integer availableSeats;
    private Integer totalSeats;
    private String amenities;
    private String destinationImageUrl;
    private String destinationHighlight;

    public static TripSearchResultDto fromEntity(BusTrip trip) {
        TripSearchResultDto dto = new TripSearchResultDto();
        dto.tripId = trip.getId();
        dto.tripCode = trip.getTripCode();
        if (trip.getBusCompany() != null) {
            dto.companyName = trip.getBusCompany().getName();
            dto.companyRating = trip.getBusCompany().getRating();
        }
        dto.busTypeName = trip.getBusType() != null ? trip.getBusType().getDisplayName() : "";
        dto.departureProvince = trip.getDepartureProvince();
        dto.departureStation = trip.getDepartureStation();
        dto.arrivalProvince = trip.getArrivalProvince();
        dto.arrivalStation = trip.getArrivalStation();
        dto.departureTimeText = DateUtils.formatDateTime(trip.getDepartureTime());
        dto.arrivalTimeText = DateUtils.formatDateTime(trip.getArrivalTime());
        dto.durationText = DateUtils.formatDuration(trip.getDepartureTime(), trip.getArrivalTime());
        dto.timeSlotLabel = DateUtils.getTimeSlotLabel(DateUtils.classifyTimeSlotCode(trip.getDepartureTime()));
        dto.ticketPrice = trip.getTicketPrice();
        dto.formattedPrice = CurrencyFormatter.formatVnd(trip.getTicketPrice());
        dto.priceTier = CurrencyFormatter.classifyPriceTier(trip.getTicketPrice());
        dto.availableSeats = trip.getAvailableSeats();
        dto.totalSeats = trip.getTotalSeats();
        dto.amenities = trip.getAmenities();
        dto.destinationImageUrl = trip.getDestinationImageUrl();
        dto.destinationHighlight = trip.getDestinationHighlight();
        return dto;
    }

    public Long getTripId() {
        return tripId;
    }

    public String getTripCode() {
        return tripCode;
    }

    public String getCompanyName() {
        return companyName;
    }

    public Double getCompanyRating() {
        return companyRating;
    }

    public String getBusTypeName() {
        return busTypeName;
    }

    public String getDepartureProvince() {
        return departureProvince;
    }

    public String getDepartureStation() {
        return departureStation;
    }

    public String getArrivalProvince() {
        return arrivalProvince;
    }

    public String getArrivalStation() {
        return arrivalStation;
    }

    public String getDepartureTimeText() {
        return departureTimeText;
    }

    public String getArrivalTimeText() {
        return arrivalTimeText;
    }

    public String getDurationText() {
        return durationText;
    }

    public String getTimeSlotLabel() {
        return timeSlotLabel;
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public String getFormattedPrice() {
        return formattedPrice;
    }

    public String getPriceTier() {
        return priceTier;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public String getAmenities() {
        return amenities;
    }

    public String getDestinationImageUrl() {
        return destinationImageUrl;
    }

    public String getDestinationHighlight() {
        return destinationHighlight;
    }
}
