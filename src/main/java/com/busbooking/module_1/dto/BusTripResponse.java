package com.busbooking.module_1.dto;

import com.busbooking.common.enums.BusType;
import com.busbooking.common.enums.TripStatus;
import com.busbooking.common.utils.CurrencyUtils;
import com.busbooking.module_1.model.BusTrip;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO phản hồi chi tiết chuyến xe phục vụ API điều phối Admin & tích hợp liên module.
 */
public class BusTripResponse {

    private Long id;
    private String tripCode;
    private Long companyId;
    private String companyName;
    private String companyHotline;
    private String routeName;
    private String departureProvince;
    private String departureStation;
    private String arrivalProvince;
    private String arrivalStation;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String durationDisplay;
    private BigDecimal ticketPrice;
    private String formattedPrice;
    private BusType busType;
    private String busTypeName;
    private String licensePlate;
    private Integer totalSeats;
    private Integer availableSeats;
    private double occupancyPercent;
    private TripStatus status;
    private String statusDisplay;
    private String amenities;
    private String destinationImageUrl;
    private boolean bookable;

    public static BusTripResponse fromEntity(BusTrip trip) {
        BusTripResponse dto = new BusTripResponse();
        dto.id = trip.getId();
        dto.tripCode = trip.getTripCode();
        if (trip.getBusCompany() != null) {
            dto.companyId = trip.getBusCompany().getId();
            dto.companyName = trip.getBusCompany().getName();
            dto.companyHotline = trip.getBusCompany().getHotline();
        }
        dto.routeName = trip.getRouteName();
        dto.departureProvince = trip.getDepartureProvince();
        dto.departureStation = trip.getDepartureStation();
        dto.arrivalProvince = trip.getArrivalProvince();
        dto.arrivalStation = trip.getArrivalStation();
        dto.departureTime = trip.getDepartureTime();
        dto.arrivalTime = trip.getArrivalTime();
        dto.durationDisplay = trip.getFormattedDuration();
        dto.ticketPrice = trip.getTicketPrice();
        dto.formattedPrice = CurrencyUtils.formatVnd(trip.getTicketPrice());
        dto.busType = trip.getBusType();
        dto.busTypeName = trip.getBusType() != null ? trip.getBusType().getDisplayName() : "";
        dto.licensePlate = trip.getLicensePlate();
        dto.totalSeats = trip.getTotalSeats();
        dto.availableSeats = trip.getAvailableSeats();
        dto.occupancyPercent = trip.getOccupancyPercent();
        dto.status = trip.getStatus();
        dto.statusDisplay = trip.getStatus() != null ? trip.getStatus().getDisplayName() : "";
        dto.amenities = trip.getAmenities();
        dto.destinationImageUrl = trip.getDestinationImageUrl();
        dto.bookable = trip.isBookable();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public String getTripCode() {
        return tripCode;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getCompanyHotline() {
        return companyHotline;
    }

    public String getRouteName() {
        return routeName;
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

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public String getDurationDisplay() {
        return durationDisplay;
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public String getFormattedPrice() {
        return formattedPrice;
    }

    public BusType getBusType() {
        return busType;
    }

    public String getBusTypeName() {
        return busTypeName;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public double getOccupancyPercent() {
        return occupancyPercent;
    }

    public TripStatus getStatus() {
        return status;
    }

    public String getStatusDisplay() {
        return statusDisplay;
    }

    public String getAmenities() {
        return amenities;
    }

    public String getDestinationImageUrl() {
        return destinationImageUrl;
    }

    public boolean isBookable() {
        return bookable;
    }
}
