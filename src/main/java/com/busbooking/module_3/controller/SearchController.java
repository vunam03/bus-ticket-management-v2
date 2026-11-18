package com.busbooking.module_3.controller;

import com.busbooking.common.enums.BusType;
import com.busbooking.module_1.model.BusTrip;
import com.busbooking.module_1.service.BusCompanyService;
import com.busbooking.module_3.dto.SearchFilterRequest;
import com.busbooking.module_3.dto.TripSearchFilterDto;
import com.busbooking.module_3.dto.TripSearchResultDto;
import com.busbooking.module_3.service.FilterService;
import com.busbooking.module_3.service.SearchService;
import com.busbooking.module_3.service.TripSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller mở rộng cung cấp điểm truy cập /search và các REST API tra cứu chuyến xe (Module 3).
 */
@Controller
public class SearchController {

    private final SearchService searchService;
    private final TripSearchService tripSearchService;
    private final FilterService filterService;
    private final BusCompanyService busCompanyService;

    public SearchController(SearchService searchService,
                            TripSearchService tripSearchService,
                            FilterService filterService,
                            BusCompanyService busCompanyService) {
        this.searchService = searchService;
        this.tripSearchService = tripSearchService;
        this.filterService = filterService;
        this.busCompanyService = busCompanyService;
    }

    /**
     * Đường dẫn tìm kiếm rút gọn (/search) phục vụ điều hướng nhanh từ trang chi tiết tuyến.
     */
    @GetMapping("/search")
    public String searchPage(@ModelAttribute("searchFilter") SearchFilterRequest filterRequest, Model model) {
        TripSearchFilterDto filter = filterRequest.toFilterDto();
        List<BusTrip> trips = tripSearchService.searchAndFilterTrips(filter);
        model.addAttribute("searchFilter", filter);
        model.addAttribute("trips", trips);
        model.addAttribute("resultCount", trips.size());
        model.addAttribute("timeSlotCounts", filterService.countByTimeSlot(trips));
        model.addAttribute("priceSummary", filterService.calculatePriceSummary(trips));
        model.addAttribute("departureProvinces", tripSearchService.getAvailableDepartureProvinces());
        model.addAttribute("arrivalProvinces", tripSearchService.getAvailableArrivalProvinces());
        model.addAttribute("companies", busCompanyService.getActiveCompanies());
        model.addAttribute("busTypes", BusType.values());
        return "module_3/search-results";
    }

    /**
     * API JSON tìm kiếm chuyến xe theo bộ lọc nâng cao kèm thống kê facets.
     */
    @GetMapping("/api/trips/search")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchTripsApi(@ModelAttribute SearchFilterRequest request) {
        TripSearchFilterDto filter = request.toFilterDto();
        List<TripSearchResultDto> results = searchService.searchTripsAsDto(request);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("results", results);
        payload.put("facets", searchService.buildSearchFacets(filter));
        return ResponseEntity.ok(payload);
    }

    /**
     * API JSON gợi ý nhanh chuyến xe theo từ khóa tìm kiếm tức thì.
     */
    @GetMapping("/api/trips/suggestions")
    @ResponseBody
    public ResponseEntity<List<TripSearchResultDto>> quickSuggestionsApi(
            @RequestParam(value = "q", required = false) String keyword) {
        return ResponseEntity.ok(searchService.findQuickSuggestions(keyword));
    }

    /**
     * API JSON trả về danh mục tỉnh/thành xuất phát và điểm đến hiện đang hoạt động.
     */
    @GetMapping("/api/trips/provinces")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> availableProvincesApi() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("departureProvinces", tripSearchService.getAvailableDepartureProvinces());
        payload.put("arrivalProvinces", tripSearchService.getAvailableArrivalProvinces());
        return ResponseEntity.ok(payload);
    }
}
