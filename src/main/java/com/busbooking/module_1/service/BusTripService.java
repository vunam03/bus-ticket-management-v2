package com.busbooking.module_1.service;

import com.busbooking.common.enums.BusType;
import com.busbooking.module_4.enums.SeatStatus;
import com.busbooking.common.enums.TripStatus;
import com.busbooking.common.exception.ResourceNotFoundException;
import com.busbooking.common.utils.CodeGeneratorUtils;
import com.busbooking.module_1.dto.BusTripFormDto;
import com.busbooking.module_1.dto.BusTripRequest;
import com.busbooking.module_1.dto.BusTripResponse;
import com.busbooking.module_1.dto.BusTripSearchCriteria;
import com.busbooking.module_1.exception.TripManagementException;
import com.busbooking.module_1.model.BusCompany;
import com.busbooking.module_1.model.BusTrip;
import com.busbooking.module_1.repository.BusTripRepository;
import com.busbooking.module_1.repository.TripSeatGeneratorRepository;
import com.busbooking.module_1.validator.BusTripValidator;
import com.busbooking.module_4.model.Seat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BusTripService {

    private final BusTripRepository tripRepository;
    private final TripSeatGeneratorRepository seatGeneratorRepository;
    private final BusCompanyService companyService;
    private final BusTripValidator tripValidator;

    public BusTripService(BusTripRepository tripRepository,
                          TripSeatGeneratorRepository seatGeneratorRepository,
                          BusCompanyService companyService,
                          BusTripValidator tripValidator) {
        this.tripRepository = tripRepository;
        this.seatGeneratorRepository = seatGeneratorRepository;
        this.companyService = companyService;
        this.tripValidator = tripValidator;
    }

    @Transactional(readOnly = true)
    public List<BusTrip> getAllTrips() {
        return tripRepository.findAllByOrderByDepartureTimeDesc();
    }

    @Transactional(readOnly = true)
    public Page<BusTrip> searchAdminTrips(BusTripSearchCriteria criteria) {
        BusTripSearchCriteria safe = criteria != null ? criteria : new BusTripSearchCriteria();
        Sort sort = resolveAdminSort(safe.getSortBy());
        Pageable pageable = PageRequest.of(safe.getPage(), safe.getSize(), sort);

        LocalDateTime dayStart = safe.getDepartureDate() != null ? safe.getDepartureDate().atStartOfDay() : null;
        LocalDateTime dayEnd = safe.getDepartureDate() != null ? safe.getDepartureDate().plusDays(1).atStartOfDay() : null;

        return tripRepository.searchAdminTrips(
                safe.getNormalizedKeyword(),
                safe.getCompanyId(),
                safe.getStatus(),
                safe.getBusType(),
                safe.getMinPrice(),
                safe.getMaxPrice(),
                dayStart,
                dayEnd,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public BusTrip getTripById(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến xe với ID: " + id));
    }

    @Transactional(readOnly = true)
    public BusTripResponse getTripResponseById(Long id) {
        return BusTripResponse.fromEntity(getTripById(id));
    }

    @Transactional
    public BusTrip createTrip(BusTripRequest request) {
        return createTrip(request.toFormDto());
    }

    /**
     * Tạo mới chuyến xe và tự động sinh sơ đồ ghế (36 / 40 / 45 ghế) theo cấu hình BusType.
     */
    @Transactional
    public BusTrip createTrip(BusTripFormDto dto) {
        tripValidator.validateTripForm(dto);
        BusCompany company = companyService.getCompanyById(dto.getCompanyId());
        if (!company.isActive()) {
            throw TripManagementException.companyInactive(company.getName());
        }

        BusType busType = dto.getBusType();
        int totalSeats = busType.getDefaultSeats();

        BusTrip trip = new BusTrip();
        trip.setTripCode(CodeGeneratorUtils.generateTripCode());
        applyTripDetails(trip, company, dto);
        trip.setBusType(busType);
        trip.setTotalSeats(totalSeats);
        trip.setAvailableSeats(totalSeats);
        trip.setStatus(dto.getStatus() != null ? dto.getStatus() : TripStatus.SCHEDULED);

        BusTrip savedTrip = tripRepository.save(trip);
        generateSeatDiagram(savedTrip);
        return savedTrip;
    }

    /**
     * Cập nhật thông tin chuyến xe. Nếu chưa có ghế nào được đặt và đổi loại xe, tự sinh lại sơ đồ ghế.
     */
    @Transactional
    public BusTrip updateTrip(Long id, BusTripFormDto dto) {
        tripValidator.validateTripForm(dto);
        BusTrip trip = getTripById(id);
        BusCompany company = companyService.getCompanyById(dto.getCompanyId());

        boolean busTypeChanged = trip.getBusType() != dto.getBusType();
        boolean priceChanged = trip.getTicketPrice().compareTo(dto.getTicketPrice()) != 0;
        long activeTickets = tripRepository.countActiveTicketsByTripId(id);

        if (busTypeChanged && activeTickets > 0) {
            throw TripManagementException.lockedByActiveTickets(trip.getTripCode());
        }

        applyTripDetails(trip, company, dto);
        if (dto.getStatus() != null) {
            trip.setStatus(dto.getStatus());
        }

        if (busTypeChanged) {
            trip.setBusType(dto.getBusType());
            trip.setTotalSeats(dto.getBusType().getDefaultSeats());
            trip.setAvailableSeats(dto.getBusType().getDefaultSeats());
            seatGeneratorRepository.deleteByBusTripId(id);
            seatGeneratorRepository.flush();
            generateSeatDiagram(trip);
        } else if (priceChanged) {
            List<Seat> seats = seatGeneratorRepository.findByBusTripIdOrderByFloorNumberAscRowIndexAscColIndexAsc(id);
            for (Seat s : seats) {
                if (s.getStatus() == SeatStatus.AVAILABLE) {
                    s.setPrice(dto.getTicketPrice());
                }
            }
            seatGeneratorRepository.saveAll(seats);
        }

        return tripRepository.save(trip);
    }

    /**
     * Nhân bản một chuyến xe sang ngày khởi hành tiếp theo để Admin lập lịch nhanh.
     */
    @Transactional
    public BusTrip duplicateTrip(Long sourceTripId, int plusDays) {
        BusTrip source = getTripById(sourceTripId);
        int daysOffset = Math.max(1, Math.min(plusDays, 30));
        Duration tripDuration = Duration.between(source.getDepartureTime(), source.getArrivalTime());

        BusTripFormDto dto = toFormDto(source);
        dto.setId(null);
        LocalDateTime nextDeparture = source.getDepartureTime().plusDays(daysOffset);
        dto.setDepartureTime(nextDeparture);
        dto.setArrivalTime(nextDeparture.plus(tripDuration));
        dto.setStatus(TripStatus.SCHEDULED);
        return createTrip(dto);
    }

    @Transactional
    public BusTrip updateTripStatus(Long id, TripStatus newStatus) {
        if (newStatus == null) {
            throw TripManagementException.invalidSchedule("Trạng thái chuyến xe mới không được để trống.");
        }
        BusTrip trip = getTripById(id);
        trip.setStatus(newStatus);
        return tripRepository.save(trip);
    }

    /**
     * Hủy chuyến xe an toàn: chuyển trạng thái sang CANCELLED, giữ nguyên dữ liệu vé của khách để đối soát/hoàn tiền.
     */
    @Transactional
    public BusTrip cancelTrip(Long id) {
        BusTrip trip = getTripById(id);
        trip.setStatus(TripStatus.CANCELLED);
        return tripRepository.save(trip);
    }

    /**
     * Xóa chuyến xe nếu chưa có vé nào được đặt; nếu đã có vé thì chuyển sang Hủy chuyến để bảo toàn lịch sử vé.
     */
    @Transactional
    public boolean deleteOrCancelTrip(Long id) {
        BusTrip trip = getTripById(id);
        long ticketCount = tripRepository.countActiveTicketsByTripId(id);
        if (ticketCount > 0) {
            trip.setStatus(TripStatus.CANCELLED);
            tripRepository.save(trip);
            return false;
        }
        seatGeneratorRepository.deleteByBusTripId(id);
        tripRepository.delete(trip);
        return true;
    }

    /**
     * Tự động sinh sơ đồ ghế 36, 40 hoặc 45 ghế theo cấu hình chuyến xe.
     */
    public void generateSeatDiagram(BusTrip trip) {
        int totalSeats = trip.getTotalSeats();
        int floors = trip.getBusType().getFloors();
        BigDecimal price = trip.getTicketPrice();
        List<Seat> seats = new ArrayList<>(totalSeats);

        if (floors == 2) {
            int seatsPerFloor = totalSeats / 2;
            int cols = 3;
            for (int floor = 1; floor <= 2; floor++) {
                String prefix = (floor == 1) ? "A" : "B";
                for (int i = 1; i <= seatsPerFloor; i++) {
                    int row = ((i - 1) / cols) + 1;
                    int col = ((i - 1) % cols) + 1;
                    String seatNumber = String.format("%s%02d", prefix, i);
                    seats.add(new Seat(trip, seatNumber, floor, row, col, price));
                }
            }
        } else {
            int cols = 4;
            for (int i = 1; i <= totalSeats; i++) {
                int row = ((i - 1) / cols) + 1;
                int col = ((i - 1) % cols) + 1;
                String seatNumber = String.format("G%02d", i);
                seats.add(new Seat(trip, seatNumber, 1, row, col, price));
            }
        }
        seatGeneratorRepository.saveAll(seats);
    }

    @Transactional
    public void refreshAvailableSeats(Long tripId) {
        BusTrip trip = getTripById(tripId);
        long available = seatGeneratorRepository.countByBusTripIdAndStatus(tripId, SeatStatus.AVAILABLE);
        trip.setAvailableSeats((int) available);
        tripRepository.save(trip);
    }

    @Transactional(readOnly = true)
    public BusTripFormDto toFormDto(BusTrip trip) {
        BusTripFormDto form = new BusTripFormDto();
        form.setId(trip.getId());
        form.setCompanyId(trip.getBusCompany().getId());
        form.setDepartureProvince(trip.getDepartureProvince());
        form.setDepartureStation(trip.getDepartureStation());
        form.setArrivalProvince(trip.getArrivalProvince());
        form.setArrivalStation(trip.getArrivalStation());
        form.setDepartureTime(trip.getDepartureTime());
        form.setArrivalTime(trip.getArrivalTime());
        form.setTicketPrice(trip.getTicketPrice());
        form.setBusType(trip.getBusType());
        form.setLicensePlate(trip.getLicensePlate());
        form.setAmenities(trip.getAmenities());
        form.setStatus(trip.getStatus());
        return form;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAdminTripSummaryStats() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalTrips", tripRepository.count());
        summary.put("scheduledTrips", tripRepository.countByStatus(TripStatus.SCHEDULED));
        summary.put("inProgressTrips", tripRepository.countByStatus(TripStatus.IN_PROGRESS));
        summary.put("completedTrips", tripRepository.countByStatus(TripStatus.COMPLETED));
        summary.put("cancelledTrips", tripRepository.countByStatus(TripStatus.CANCELLED));
        summary.put("openAvailableSeats", tripRepository.sumAvailableSeatsByStatus(TripStatus.SCHEDULED));
        return summary;
    }

    private void applyTripDetails(BusTrip trip, BusCompany company, BusTripFormDto dto) {
        trip.setBusCompany(company);
        trip.setDepartureProvince(dto.getDepartureProvince().trim());
        trip.setDepartureStation(dto.getDepartureStation().trim());
        trip.setArrivalProvince(dto.getArrivalProvince().trim());
        trip.setArrivalStation(dto.getArrivalStation().trim());
        trip.setRouteName(dto.getDepartureProvince().trim() + " - " + dto.getArrivalProvince().trim());
        trip.setDepartureTime(dto.getDepartureTime());
        trip.setArrivalTime(dto.getArrivalTime());
        trip.setTicketPrice(dto.getTicketPrice());
        trip.setLicensePlate(dto.getLicensePlate().trim().toUpperCase());
        String amenities = dto.getAmenities() != null && !dto.getAmenities().isBlank()
                ? dto.getAmenities().trim()
                : "Wifi, Nước suối, Điều hòa";
        trip.setAmenities(amenities);
    }

    private Sort resolveAdminSort(String sortBy) {
        if ("PRICE_ASC".equalsIgnoreCase(sortBy)) {
            return Sort.by(Sort.Direction.ASC, "ticketPrice");
        }
        if ("PRICE_DESC".equalsIgnoreCase(sortBy)) {
            return Sort.by(Sort.Direction.DESC, "ticketPrice");
        }
        if ("SEATS_ASC".equalsIgnoreCase(sortBy)) {
            return Sort.by(Sort.Direction.ASC, "availableSeats");
        }
        if ("DEPARTURE_ASC".equalsIgnoreCase(sortBy)) {
            return Sort.by(Sort.Direction.ASC, "departureTime");
        }
        return Sort.by(Sort.Direction.DESC, "departureTime");
    }
}
