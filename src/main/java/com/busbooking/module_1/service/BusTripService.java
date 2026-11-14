package com.busbooking.module_1.service;

import com.busbooking.module_1.dto.BusTripFormDto;
import com.busbooking.module_1.repository.BusTripRepository;
import com.busbooking.module_1.repository.TripSeatGeneratorRepository;
import com.busbooking.module_1.validator.TripValidator;
import com.busbooking.shared.enums.BusType;
import com.busbooking.shared.enums.SeatStatus;
import com.busbooking.shared.enums.TripStatus;
import com.busbooking.shared.exception.BusinessException;
import com.busbooking.shared.exception.ResourceNotFoundException;
import com.busbooking.shared.model.BusCompany;
import com.busbooking.shared.model.BusTrip;
import com.busbooking.shared.model.Seat;
import com.busbooking.shared.utils.CodeGeneratorUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class BusTripService {

    private final BusTripRepository tripRepository;
    private final TripSeatGeneratorRepository seatGeneratorRepository;
    private final BusCompanyService companyService;
    private final TripValidator tripValidator;

    public BusTripService(BusTripRepository tripRepository,
                          TripSeatGeneratorRepository seatGeneratorRepository,
                          BusCompanyService companyService,
                          TripValidator tripValidator) {
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
    public BusTrip getTripById(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến xe với ID: " + id));
    }

    /**
     * Tạo mới chuyến xe và tự động sinh sơ đồ ghế (36 / 40 / 45 ghế) theo cấu hình BusType.
     */
    @Transactional
    public BusTrip createTrip(BusTripFormDto dto) {
        tripValidator.validateTripForm(dto);
        BusCompany company = companyService.getCompanyById(dto.getCompanyId());
        if (!company.isActive()) {
            throw new BusinessException("COMPANY_INACTIVE", "Nhà xe đang ngừng hoạt động, không thể thêm chuyến xe mới.");
        }

        BusType busType = dto.getBusType();
        int totalSeats = busType.getDefaultSeats();

        BusTrip trip = new BusTrip();
        trip.setTripCode(CodeGeneratorUtils.generateTripCode());
        trip.setBusCompany(company);
        trip.setDepartureProvince(dto.getDepartureProvince().trim());
        trip.setDepartureStation(dto.getDepartureStation().trim());
        trip.setArrivalProvince(dto.getArrivalProvince().trim());
        trip.setArrivalStation(dto.getArrivalStation().trim());
        trip.setRouteName(dto.getDepartureProvince().trim() + " - " + dto.getArrivalProvince().trim());
        trip.setDepartureTime(dto.getDepartureTime());
        trip.setArrivalTime(dto.getArrivalTime());
        trip.setTicketPrice(dto.getTicketPrice());
        trip.setBusType(busType);
        trip.setLicensePlate(dto.getLicensePlate().trim().toUpperCase());
        trip.setTotalSeats(totalSeats);
        trip.setAvailableSeats(totalSeats);
        trip.setStatus(TripStatus.SCHEDULED);
        trip.setAmenities(dto.getAmenities() != null ? dto.getAmenities().trim() : "Wifi, Nước suối, Điều hòa");

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
            throw new BusinessException("CANNOT_CHANGE_BUS_TYPE",
                    "Chuyến xe đã có khách đặt vé, không thể thay đổi loại sơ đồ ghế.");
        }

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
        trip.setAmenities(dto.getAmenities() != null ? dto.getAmenities().trim() : trip.getAmenities());
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
            return false; // Đã chuyển sang hủy chuyến thay vì xóa cứng
        }
        seatGeneratorRepository.deleteByBusTripId(id);
        tripRepository.delete(trip);
        return true; // Đã xóa hoàn toàn
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
            // Xe giường nằm 2 tầng (36 hoặc 40 chỗ): Tầng 1 (A01..A18/20), Tầng 2 (B01..B18/20)
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
            // Xe ghế ngồi 1 tầng (45 chỗ): 4 cột mỗi hàng + băng cuối
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
}
