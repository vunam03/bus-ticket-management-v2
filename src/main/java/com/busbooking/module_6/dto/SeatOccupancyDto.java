package com.busbooking.module_6.dto;

import java.time.LocalDateTime;

public class SeatOccupancyDto {

    private Long tripId;
    private String tripCode;
    private String routeName;
    private String companyName;
    private LocalDateTime departureTime;
    private int totalSeats;
    private int bookedSeats;
    private int heldSeats;
    private int availableSeats;
    private double occupancyRate;

    public SeatOccupancyDto() {
    }

    public SeatOccupancyDto(Long tripId, String tripCode, String routeName, String companyName,
                            LocalDateTime departureTime, int totalSeats, int bookedSeats,
                            int heldSeats, int availableSeats, double occupancyRate) {
        this.tripId = tripId;
        this.tripCode = tripCode;
        this.routeName = routeName;
        this.companyName = companyName;
        this.departureTime = departureTime;
        this.totalSeats = totalSeats;
        this.bookedSeats = bookedSeats;
        this.heldSeats = heldSeats;
        this.availableSeats = availableSeats;
        this.occupancyRate = occupancyRate;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public String getTripCode() {
        return tripCode;
    }

    public void setTripCode(String tripCode) {
        this.tripCode = tripCode;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getBookedSeats() {
        return bookedSeats;
    }

    public void setBookedSeats(int bookedSeats) {
        this.bookedSeats = bookedSeats;
    }

    public int getHeldSeats() {
        return heldSeats;
    }

    public void setHeldSeats(int heldSeats) {
        this.heldSeats = heldSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public double getOccupancyRate() {
        return occupancyRate;
    }

    public void setOccupancyRate(double occupancyRate) {
        this.occupancyRate = occupancyRate;
    }
}
