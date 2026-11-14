package com.busbooking.module_1;

import com.busbooking.module_1.dto.BusCompanyFormDto;
import com.busbooking.module_1.dto.BusTripFormDto;
import com.busbooking.module_1.repository.TripSeatGeneratorRepository;
import com.busbooking.module_1.service.BusCompanyService;
import com.busbooking.module_1.service.BusTripService;
import com.busbooking.shared.enums.BusType;
import com.busbooking.shared.enums.TripStatus;
import com.busbooking.shared.exception.BusinessException;
import com.busbooking.shared.model.BusCompany;
import com.busbooking.shared.model.BusTrip;
import com.busbooking.shared.model.Seat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class Module1TripCompanyTest {

    @Autowired
    private BusCompanyService companyService;

    @Autowired
    private BusTripService tripService;

    @Autowired
    private TripSeatGeneratorRepository seatRepository;

    @Test
    @DisplayName("Module 1: Tạo nhà xe mới và bật/tắt trạng thái hoạt động")
    void shouldCreateAndToggleBusCompany() {
        BusCompanyFormDto dto = new BusCompanyFormDto();
        dto.setName("Nhà xe Kiểm thử Quốc Gia");
        dto.setHotline("19001234");
        dto.setHeadquarters("Bến xe Giáp Bát, Hà Nội");
        dto.setDescription("Nhà xe chất lượng cao");
        dto.setRating(4.9);
        dto.setActive(true);

        BusCompany saved = companyService.createCompany(dto);
        assertNotNull(saved.getId());
        assertEquals("Nhà xe Kiểm thử Quốc Gia", saved.getName());
        assertTrue(saved.isActive());

        companyService.toggleCompanyStatus(saved.getId());
        BusCompany toggled = companyService.getCompanyById(saved.getId());
        assertFalse(toggled.isActive());
    }

    @Test
    @DisplayName("Module 1: Tạo chuyến xe mới tự động sinh đúng sơ đồ 36/40/45 ghế")
    void shouldAutoGenerateSeatDiagramWhenCreatingTrip() {
        List<BusCompany> companies = companyService.getActiveCompanies();
        assertFalse(companies.isEmpty());

        BusTripFormDto tripDto = new BusTripFormDto();
        tripDto.setCompanyId(companies.get(0).getId());
        tripDto.setDepartureProvince("Hà Nội");
        tripDto.setArrivalProvince("Nha Trang");
        tripDto.setDepartureStation("Bến xe Nước Ngầm");
        tripDto.setArrivalStation("Bến xe Phía Nam Nha Trang");
        tripDto.setDepartureTime(LocalDateTime.now().plusDays(3));
        tripDto.setArrivalTime(LocalDateTime.now().plusDays(3).plusHours(14));
        tripDto.setTicketPrice(new BigDecimal("520000"));
        tripDto.setBusType(BusType.SLEEPER_40);
        tripDto.setLicensePlate("29B-999.88");
        tripDto.setStatus(TripStatus.SCHEDULED);

        BusTrip createdTrip = tripService.createTrip(tripDto);
        assertNotNull(createdTrip.getId());
        assertEquals(40, createdTrip.getTotalSeats());
        assertEquals(40, createdTrip.getAvailableSeats());

        List<Seat> seats = seatRepository.findByBusTripIdOrderByFloorNumberAscRowIndexAscColIndexAsc(createdTrip.getId());
        assertEquals(40, seats.size());
    }

    @Test
    @DisplayName("Module 1: Chặn tạo chuyến xe có điểm đi trùng điểm đến")
    void shouldRejectSameDepartureAndArrivalProvince() {
        List<BusCompany> companies = companyService.getActiveCompanies();
        BusTripFormDto tripDto = new BusTripFormDto();
        tripDto.setCompanyId(companies.get(0).getId());
        tripDto.setDepartureProvince("Hà Nội");
        tripDto.setArrivalProvince("Hà Nội");
        tripDto.setDepartureStation("Bến A");
        tripDto.setArrivalStation("Bến B");
        tripDto.setDepartureTime(LocalDateTime.now().plusDays(1));
        tripDto.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(3));
        tripDto.setTicketPrice(new BigDecimal("200000"));
        tripDto.setBusType(BusType.LIMOUSINE_36);
        tripDto.setLicensePlate("30A-111.22");

        assertThrows(BusinessException.class, () -> tripService.createTrip(tripDto));
    }
}
