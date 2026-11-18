package com.busbooking.module_3.controller;

import com.busbooking.common.enums.BusType;
import com.busbooking.module_1.model.BusTrip;
import com.busbooking.module_1.service.BusCompanyService;
import com.busbooking.module_3.dto.TripSearchFilterDto;
import com.busbooking.module_3.service.FilterService;
import com.busbooking.module_3.service.RouteService;
import com.busbooking.module_3.service.TripSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Controller trang chủ, tìm kiếm và xem chi tiết chuyến xe dành cho khách hàng (Module 3).
 */
@Controller
public class HomeSearchController {

    private final TripSearchService searchService;
    private final BusCompanyService companyService;
    private final RouteService routeService;
    private final FilterService filterService;

    public HomeSearchController(TripSearchService searchService, BusCompanyService companyService) {
        this(searchService, companyService, null, new FilterService());
    }

    @Autowired
    public HomeSearchController(TripSearchService searchService,
                                BusCompanyService companyService,
                                RouteService routeService,
                                FilterService filterService) {
        this.searchService = searchService;
        this.companyService = companyService;
        this.routeService = routeService;
        this.filterService = filterService != null ? filterService : new FilterService();
    }

    @GetMapping("/")
    public String homePage(Model model) {
        TripSearchFilterDto filter = new TripSearchFilterDto();
        model.addAttribute("searchFilter", filter);
        model.addAttribute("featuredTrips", searchService.getFeaturedTrips());
        model.addAttribute("departureProvinces", searchService.getAvailableDepartureProvinces());
        model.addAttribute("arrivalProvinces", searchService.getAvailableArrivalProvinces());
        model.addAttribute("companies", companyService.getActiveCompanies());
        if (routeService != null) {
            model.addAttribute("popularRoutes", routeService.getPopularRouteRecommendations());
        }
        return "module_3/home";
    }

    @GetMapping("/trips/search")
    public String searchTrips(@ModelAttribute("searchFilter") TripSearchFilterDto filter, Model model) {
        List<BusTrip> results = searchService.searchAndFilterTrips(filter);
        model.addAttribute("trips", results);
        model.addAttribute("resultCount", results.size());
        model.addAttribute("timeSlotCounts", filterService.countByTimeSlot(results));
        model.addAttribute("priceSummary", filterService.calculatePriceSummary(results));
        model.addAttribute("departureProvinces", searchService.getAvailableDepartureProvinces());
        model.addAttribute("arrivalProvinces", searchService.getAvailableArrivalProvinces());
        model.addAttribute("companies", companyService.getActiveCompanies());
        model.addAttribute("busTypes", BusType.values());
        return "module_3/search-results";
    }

    @GetMapping("/trips/{id}")
    public String tripDetail(@PathVariable Long id, Model model) {
        BusTrip trip = searchService.getTripDetail(id);
        model.addAttribute("trip", trip);
        return "module_3/trip-detail";
    }
}
