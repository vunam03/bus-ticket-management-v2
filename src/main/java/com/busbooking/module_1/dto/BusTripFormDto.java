package com.busbooking.module_1.dto;

import com.busbooking.common.enums.BusType;
import com.busbooking.common.enums.TripStatus;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BusTripFormDto {

    private Long id;

    @NotNull(message = "Vui lòng chọn nhà xe vận hành")
    private Long companyId;

    @NotBlank(message = "Tỉnh/Thành phố đi không được để trống")
    private String departureProvince;

    @NotBlank(message = "Bến xe xuất phát không được để trống")
    private String departureStation;

    @NotBlank(message = "Tỉnh/Thành phố đến không được để trống")
    private String arrivalProvince;

    @NotBlank(message = "Bến xe đích không được để trống")
    private String arrivalStation;

    @NotNull(message = "Thời gian khởi hành không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime departureTime;

    @NotNull(message = "Thời gian dự kiến đến không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime arrivalTime;

    @NotNull(message = "Giá vé không được để trống")
    @DecimalMin(value = "10000", message = "Giá vé tối thiểu là 10.000 VNĐ")
    private BigDecimal ticketPrice;

    @NotNull(message = "Vui lòng chọn loại xe và sơ đồ ghế")
    private BusType busType = BusType.SLEEPER_40;

    @NotBlank(message = "Biển số xe không được để trống")
    private String licensePlate;

    private String amenities = "Wifi tốc độ cao, Nước suối, Khăn lạnh, Cổng sạc USB, Điều hòa";

    private TripStatus status = TripStatus.SCHEDULED;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getDepartureProvince() {
        return departureProvince;
    }

    public void setDepartureProvince(String departureProvince) {
        this.departureProvince = departureProvince;
    }

    public String getDepartureStation() {
        return departureStation;
    }

    public void setDepartureStation(String departureStation) {
        this.departureStation = departureStation;
    }

    public String getArrivalProvince() {
        return arrivalProvince;
    }

    public void setArrivalProvince(String arrivalProvince) {
        this.arrivalProvince = arrivalProvince;
    }

    public String getArrivalStation() {
        return arrivalStation;
    }

    public void setArrivalStation(String arrivalStation) {
        this.arrivalStation = arrivalStation;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(BigDecimal ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public BusType getBusType() {
        return busType;
    }

    public void setBusType(BusType busType) {
        this.busType = busType;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getAmenities() {
        return amenities;
    }

    public void setAmenities(String amenities) {
        this.amenities = amenities;
    }

    public TripStatus getStatus() {
        return status;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }
}
