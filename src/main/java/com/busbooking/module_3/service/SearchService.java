package com.busbooking.module_3.service;

import com.busbooking.module_1.model.BusTrip;
import com.busbooking.module_1.service.BusCompanyService;
import com.busbooking.module_3.dto.SearchFilterRequest;
import com.busbooking.module_3.dto.TripSearchFilterDto;
import com.busbooking.module_3.dto.TripSearchResultDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dịch vụ tổng hợp tìm kiếm chuyến xe dạng DTO, bộ đếm facet và gợi ý nhanh (Module 3).
 */
@Service
public class SearchService {

    private final TripSearchService tripSearchService;
    private final FilterService filterService;
    private final BusCompanyService busCompanyService;

    public SearchService(TripSearchService tripSearchService,
                         FilterService filterService,
                         BusCompanyService busCompanyService) {
        this.tripSearchService = tripSearchService;
        this.filterService = filterService;
        this.busCompanyService = busCompanyService;
    }

    /**
     * Thực thi tìm kiếm chuyến xe theo SearchFilterRequest và ánh xạ sang TripSearchResultDto.
     */
    @Transactional(readOnly = true)
    public List<TripSearchResultDto> searchTripsAsDto(SearchFilterRequest request) {
        TripSearchFilterDto filter = request != null ? request.toFilterDto() : new TripSearchFilterDto();
        return tripSearchService.searchAndFilterTrips(filter).stream()
                .map(TripSearchResultDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Xây dựng thông tin thống kê (facets) đi kèm kết quả tìm kiếm cho giao diện và REST API.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> buildSearchFacets(TripSearchFilterDto filter) {
        List<BusTrip> matchedTrips = tripSearchService.searchAndFilterTrips(filter);
        Map<String, Object> facets = new LinkedHashMap<>();
        facets.put("totalResults", matchedTrips.size());
        facets.put("timeSlotCounts", filterService.countByTimeSlot(matchedTrips));
        facets.put("busTypeCounts", filterService.countByBusType(matchedTrips));
        facets.put("priceSummary", filterService.calculatePriceSummary(matchedTrips));
        facets.put("activeCompanyCount", busCompanyService.getActiveCompanies().size());
        return facets;
    }

    /**
     * Gợi ý nhanh các chuyến xe khớp với từ khóa tên tuyến, điểm đi, điểm đến hoặc nhà xe.
     */
    @Transactional(readOnly = true)
    public List<TripSearchResultDto> findQuickSuggestions(String keyword) {
        List<BusTrip> scheduled = tripSearchService.searchAndFilterTrips(new TripSearchFilterDto());
        if (keyword == null || keyword.isBlank()) {
            return scheduled.stream()
                    .limit(6)
                    .map(TripSearchResultDto::fromEntity)
                    .collect(Collectors.toList());
        }
        String normalized = keyword.trim().toLowerCase(Locale.ROOT);
        return scheduled.stream()
                .filter(trip -> containsIgnoreCase(trip.getRouteName(), normalized)
                        || containsIgnoreCase(trip.getDepartureProvince(), normalized)
                        || containsIgnoreCase(trip.getArrivalProvince(), normalized)
                        || containsIgnoreCase(trip.getTripCode(), normalized)
                        || (trip.getBusCompany() != null && containsIgnoreCase(trip.getBusCompany().getName(), normalized)))
                .limit(8)
                .map(TripSearchResultDto::fromEntity)
                .collect(Collectors.toList());
    }

    private boolean containsIgnoreCase(String source, String normalizedKeyword) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(normalizedKeyword);
    }
}
