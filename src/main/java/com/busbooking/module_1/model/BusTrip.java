package com.busbooking.module_1.model;

import com.busbooking.common.enums.BusType;
import com.busbooking.common.enums.TripStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bus_trips", indexes = {
        @Index(name = "idx_trip_departure", columnList = "departureProvince, arrivalProvince, departureTime"),
        @Index(name = "idx_trip_status", columnList = "status")
})
public class BusTrip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String tripCode;

    @Column(nullable = false, length = 150)
    private String routeName;

    @Column(nullable = false, length = 100)
    private String departureProvince;

    @Column(nullable = false, length = 150)
    private String departureStation;

    @Column(nullable = false, length = 100)
    private String arrivalProvince;

    @Column(nullable = false, length = 150)
    private String arrivalStation;

    @Column(nullable = false)
    private LocalDateTime departureTime;

    @Column(nullable = false)
    private LocalDateTime arrivalTime;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal ticketPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BusType busType;

    @Column(nullable = false, length = 30)
    private String licensePlate;

    @Column(nullable = false)
    private Integer totalSeats;

    @Column(nullable = false)
    private Integer availableSeats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TripStatus status = TripStatus.SCHEDULED;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id", nullable = false)
    private BusCompany busCompany;

    @Column(length = 500)
    private String amenities;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public BusTrip() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = TripStatus.SCHEDULED;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    public TripStatus getStatus() {
        return status;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }

    public BusCompany getBusCompany() {
        return busCompany;
    }

    public void setBusCompany(BusCompany busCompany) {
        this.busCompany = busCompany;
    }

    public String getAmenities() {
        return amenities;
    }

    public String getNotes() {
        return amenities;
    }

    public void setAmenities(String amenities) {
        this.amenities = amenities;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @jakarta.persistence.Transient
    public long getDurationMinutes() {
        if (departureTime == null || arrivalTime == null || !arrivalTime.isAfter(departureTime)) {
            return 0;
        }
        return java.time.Duration.between(departureTime, arrivalTime).toMinutes();
    }

    @jakarta.persistence.Transient
    public String getFormattedDuration() {
        long minutes = getDurationMinutes();
        if (minutes <= 0) {
            return "Chưa xác định";
        }
        long hours = minutes / 60;
        long mins = minutes % 60;
        return mins == 0 ? (hours + " giờ") : (hours + " giờ " + mins + " phút");
    }

    @jakarta.persistence.Transient
    public boolean isBookable() {
        return status == TripStatus.SCHEDULED
                && availableSeats != null
                && availableSeats > 0
                && departureTime != null
                && departureTime.isAfter(LocalDateTime.now());
    }

    @jakarta.persistence.Transient
    public boolean isDeparted() {
        return departureTime != null && !departureTime.isAfter(LocalDateTime.now());
    }

    @jakarta.persistence.Transient
    public double getOccupancyPercent() {
        if (totalSeats == null || totalSeats <= 0 || availableSeats == null) {
            return 0.0;
        }
        int booked = Math.max(0, totalSeats - availableSeats);
        return Math.round((booked * 1000.0) / totalSeats) / 10.0;
    }

    @jakarta.persistence.Transient
    public String getDestinationImageUrl() {
        if (arrivalProvince == null) {
            return "/images/destinations/default.svg";
        }
        String p = arrivalProvince.toLowerCase(java.util.Locale.ROOT);
        if (p.contains("đà lạt") || p.contains("lâm đồng")) {
            return "/images/destinations/dalat.svg";
        }
        if (p.contains("sa pa") || p.contains("lào cai")) {
            return "/images/destinations/sapa.svg";
        }
        if (p.contains("đà nẵng")) {
            return "/images/destinations/danang.svg";
        }
        if (p.contains("nha trang") || p.contains("khánh hòa")) {
            return "/images/destinations/nhatrang.svg";
        }
        if (p.contains("huế")) {
            return "/images/destinations/hue.svg";
        }
        if (p.contains("hải phòng") || p.contains("quảng ninh") || p.contains("hạ long")) {
            return "/images/destinations/haiphong.svg";
        }
        return "/images/destinations/default.svg";
    }

    @jakarta.persistence.Transient
    public String getDestinationHighlight() {
        if (arrivalProvince == null) {
            return "Hành trình tiện nghi, an toàn trên từng cây số";
        }
        String p = arrivalProvince.toLowerCase(java.util.Locale.ROOT);
        if (p.contains("đà lạt") || p.contains("lâm đồng")) {
            return "Thành phố ngàn hoa & đồi thông mờ sương";
        }
        if (p.contains("sa pa") || p.contains("lào cai")) {
            return "Săn mây đỉnh Fansipan & ruộng bậc thang";
        }
        if (p.contains("đà nẵng")) {
            return "Cầu Rồng rực sáng & biển Mỹ Khê thơ mộng";
        }
        if (p.contains("nha trang") || p.contains("khánh hòa")) {
            return "Vịnh biển ngọc bích & nắng vàng rực rỡ";
        }
        if (p.contains("huế")) {
            return "Kinh thành cổ kính & dòng Hương Giang êm đềm";
        }
        if (p.contains("hải phòng") || p.contains("quảng ninh") || p.contains("hạ long")) {
            return "Thành phố hoa phượng đỏ & cửa ngõ vịnh biển";
        }
        return "Hành trình tiện nghi, an toàn trên từng cây số";
    }
}
