package com.busbooking.module_1.dto;

import com.busbooking.common.enums.BusType;
import com.busbooking.common.enums.TripStatus;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO nhận dữ liệu tạo/cập nhật chuyến xe từ biểu mẫu Admin và API JSON (Module 1).
 */
public class BusTripRequest {

    @NotNull(message = "Vui lòng chọn nhà xe vận hành")
    private Long companyId;

    @NotBlank(message = "Tỉnh/thành phố khởi hành không được để trống")
    @Size(max = 100, message = "Tỉnh/thành phố khởi hành tối đa 100 ký tự")
    private String departureProvince;

    @NotBlank(message = "Điểm đón/bến xe xuất phát không được để trống")
    @Size(max = 150, message = "Tên bến xe đi tối đa 150 ký tự")
    private String departureStation;

    @NotBlank(message = "Tỉnh/thành phố đến không được để trống")
    @Size(max = 100, message = "Tỉnh/thành phố đến tối đa 100 ký tự")
    private String arrivalProvince;

    @NotBlank(message = "Điểm trả/bến xe đến không được để trống")
    @Size(max = 150, message = "Tên bến xe đến tối đa 150 ký tự")
    private String arrivalStation;

    @NotNull(message = "Vui lòng chọn ngày giờ khởi hành")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime departureTime;

    @NotNull(message = "Vui lòng chọn ngày giờ dự kiến đến")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime arrivalTime;

    @NotNull(message = "Giá vé không được để trống")
    @DecimalMin(value = "10000", message = "Giá vé tối thiểu phải từ 10.000 VNĐ")
    @DecimalMax(value = "10000000", message = "Giá vé vượt quá giới hạn cho phép")
    private BigDecimal ticketPrice;

    @NotNull(message = "Vui lòng chọn loại xe")
    private BusType busType;

    @NotBlank(message = "Biển số xe không được để trống")
    @Size(max = 30, message = "Biển số xe tối đa 30 ký tự")
    private String licensePlate;

    @Size(max = 255, message = "Danh sách tiện ích tối đa 255 ký tự")
    private String amenities;

    private TripStatus status = TripStatus.SCHEDULED;

    public BusTripFormDto toFormDto() {
        BusTripFormDto form = new BusTripFormDto();
        form.setCompanyId(companyId);
        form.setDepartureProvince(departureProvince);
        form.setDepartureStation(departureStation);
        form.setArrivalProvince(arrivalProvince);
        form.setArrivalStation(arrivalStation);
        form.setDepartureTime(departureTime);
        form.setArrivalTime(arrivalTime);
        form.setTicketPrice(ticketPrice);
        form.setBusType(busType);
        form.setLicensePlate(licensePlate);
        form.setAmenities(amenities);
        form.setStatus(status != null ? status : TripStatus.SCHEDULED);
        return form;
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
