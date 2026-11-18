package com.busbooking.module_3;

import com.busbooking.module_3.dto.TripSearchFilterDto;
import com.busbooking.module_3.service.TripSearchService;
import com.busbooking.module_1.model.BusTrip;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class Module3SearchFilterTest {

    @Autowired
    private TripSearchService searchService;

    @Test
    @DisplayName("Module 3: Tìm kiếm và lọc chuyến xe theo điểm đi, khoảng giá và sắp xếp tăng dần")
    void shouldSearchAndFilterTripsByProvinceAndPrice() {
        TripSearchFilterDto filter = new TripSearchFilterDto();
        filter.setDepartureProvince("TP. Hồ Chí Minh");
        filter.setMinPrice(new BigDecimal("100000"));
        filter.setMaxPrice(new BigDecimal("500000"));
        filter.setSortBy("PRICE_ASC");

        List<BusTrip> results = searchService.searchAndFilterTrips(filter);
        assertFalse(results.isEmpty());

        for (int i = 0; i < results.size(); i++) {
            BusTrip trip = results.get(i);
            assertTrue(trip.getDepartureProvince().contains("Hồ Chí Minh"));
            if (i > 0) {
                assertTrue(trip.getTicketPrice().compareTo(results.get(i - 1).getTicketPrice()) >= 0);
            }
        }
    }

    @Test
    @DisplayName("Module 3: Lấy danh sách tỉnh đi, tỉnh đến và chuyến nổi bật")
    void shouldReturnFeaturedTripsAndDistinctProvinces() {
        List<BusTrip> featured = searchService.getFeaturedTrips();
        List<String> depProvinces = searchService.getAvailableDepartureProvinces();
        List<String> arrProvinces = searchService.getAvailableArrivalProvinces();

        assertFalse(featured.isEmpty());
        assertFalse(depProvinces.isEmpty());
        assertFalse(arrProvinces.isEmpty());
    }
}
