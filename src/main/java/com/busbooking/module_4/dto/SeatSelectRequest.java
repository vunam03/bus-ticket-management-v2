package com.busbooking.module_4.dto;

import java.util.List;

/**
 * DTO nhận yêu cầu chọn, giữ chỗ, đổi ghế hoặc tính giá cho danh sách ghế trên một chuyến xe (Module 4).
 */
public class SeatSelectRequest {

    private Long tripId;
    private List<Long> seatIds;
    private String holdCode;
    private Integer floorPreference;
    private Integer autoSuggestCount = 1;
    private Integer extendMinutes = 3;
    private String notes;

    public boolean hasSeatSelection() {
        return seatIds != null && !seatIds.isEmpty();
    }

    public boolean isWithinMaxLimit(int maxSeats) {
        return seatIds != null && seatIds.size() <= maxSeats;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public List<Long> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<Long> seatIds) {
        this.seatIds = seatIds;
    }

    public String getHoldCode() {
        return holdCode;
    }

    public void setHoldCode(String holdCode) {
        this.holdCode = holdCode;
    }

    public Integer getFloorPreference() {
        return floorPreference;
    }

    public void setFloorPreference(Integer floorPreference) {
        this.floorPreference = floorPreference;
    }

    public Integer getAutoSuggestCount() {
        return autoSuggestCount;
    }

    public void setAutoSuggestCount(Integer autoSuggestCount) {
        this.autoSuggestCount = autoSuggestCount;
    }

    public Integer getExtendMinutes() {
        return extendMinutes;
    }

    public void setExtendMinutes(Integer extendMinutes) {
        this.extendMinutes = extendMinutes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
