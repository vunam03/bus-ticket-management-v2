package com.busbooking.module_4.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO phản hồi toàn bộ bố cục sơ đồ ghế 1 hoặc 2 tầng kèm thống kê trống/đang giữ/đã đặt/không khả dụng (Module 4).
 */
public class SeatLayoutResponse {

    private Long tripId;
    private String tripCode;
    private String routeName;
    private String busTypeLabel;
    private boolean tripBookable = true;
    private String tripStatusLabel;
    private int totalFloors;
    private int totalSeats;
    private int availableCount;
    private int heldCount;
    private int bookedCount;
    private int unavailableCount;
    private int floor1AvailableCount;
    private int floor2AvailableCount;
    private BigDecimal unitPrice;
    private String formattedUnitPrice;
    private String activeHoldCode;
    private long holdRemainingSeconds;
    private boolean canExtendHold;
    private long maxExtensionRemainingSeconds;
    private List<Long> activeHoldSeatIds = new ArrayList<>();
    private List<String> activeHoldSeatNumbers = new ArrayList<>();
    private List<SeatStatusDto> floor1Seats = new ArrayList<>();
    private List<SeatStatusDto> floor2Seats = new ArrayList<>();

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

    public String getBusTypeLabel() {
        return busTypeLabel;
    }

    public void setBusTypeLabel(String busTypeLabel) {
        this.busTypeLabel = busTypeLabel;
    }

    public boolean isTripBookable() {
        return tripBookable;
    }

    public void setTripBookable(boolean tripBookable) {
        this.tripBookable = tripBookable;
    }

    public String getTripStatusLabel() {
        return tripStatusLabel;
    }

    public void setTripStatusLabel(String tripStatusLabel) {
        this.tripStatusLabel = tripStatusLabel;
    }

    public int getTotalFloors() {
        return totalFloors;
    }

    public void setTotalFloors(int totalFloors) {
        this.totalFloors = totalFloors;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getAvailableCount() {
        return availableCount;
    }

    public void setAvailableCount(int availableCount) {
        this.availableCount = availableCount;
    }

    public int getHeldCount() {
        return heldCount;
    }

    public void setHeldCount(int heldCount) {
        this.heldCount = heldCount;
    }

    public int getBookedCount() {
        return bookedCount;
    }

    public void setBookedCount(int bookedCount) {
        this.bookedCount = bookedCount;
    }

    public int getUnavailableCount() {
        return unavailableCount;
    }

    public void setUnavailableCount(int unavailableCount) {
        this.unavailableCount = unavailableCount;
    }

    public int getFloor1AvailableCount() {
        return floor1AvailableCount;
    }

    public void setFloor1AvailableCount(int floor1AvailableCount) {
        this.floor1AvailableCount = floor1AvailableCount;
    }

    public int getFloor2AvailableCount() {
        return floor2AvailableCount;
    }

    public void setFloor2AvailableCount(int floor2AvailableCount) {
        this.floor2AvailableCount = floor2AvailableCount;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getFormattedUnitPrice() {
        return formattedUnitPrice;
    }

    public void setFormattedUnitPrice(String formattedUnitPrice) {
        this.formattedUnitPrice = formattedUnitPrice;
    }

    public String getActiveHoldCode() {
        return activeHoldCode;
    }

    public void setActiveHoldCode(String activeHoldCode) {
        this.activeHoldCode = activeHoldCode;
    }

    public long getHoldRemainingSeconds() {
        return holdRemainingSeconds;
    }

    public void setHoldRemainingSeconds(long holdRemainingSeconds) {
        this.holdRemainingSeconds = holdRemainingSeconds;
    }

    public boolean isCanExtendHold() {
        return canExtendHold;
    }

    public void setCanExtendHold(boolean canExtendHold) {
        this.canExtendHold = canExtendHold;
    }

    public long getMaxExtensionRemainingSeconds() {
        return maxExtensionRemainingSeconds;
    }

    public void setMaxExtensionRemainingSeconds(long maxExtensionRemainingSeconds) {
        this.maxExtensionRemainingSeconds = maxExtensionRemainingSeconds;
    }

    public List<Long> getActiveHoldSeatIds() {
        return activeHoldSeatIds;
    }

    public void setActiveHoldSeatIds(List<Long> activeHoldSeatIds) {
        this.activeHoldSeatIds = activeHoldSeatIds;
    }

    public List<String> getActiveHoldSeatNumbers() {
        return activeHoldSeatNumbers;
    }

    public void setActiveHoldSeatNumbers(List<String> activeHoldSeatNumbers) {
        this.activeHoldSeatNumbers = activeHoldSeatNumbers;
    }

    public List<SeatStatusDto> getFloor1Seats() {
        return floor1Seats;
    }

    public void setFloor1Seats(List<SeatStatusDto> floor1Seats) {
        this.floor1Seats = floor1Seats;
    }

    public List<SeatStatusDto> getFloor2Seats() {
        return floor2Seats;
    }

    public void setFloor2Seats(List<SeatStatusDto> floor2Seats) {
        this.floor2Seats = floor2Seats;
    }
}
