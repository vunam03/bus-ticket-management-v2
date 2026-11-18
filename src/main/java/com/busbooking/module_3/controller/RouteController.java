package com.busbooking.module_3.controller;

import com.busbooking.module_1.service.BusCompanyService;
import com.busbooking.module_3.dto.RouteRecommendationDto;
import com.busbooking.module_3.dto.TripSearchFilterDto;
import com.busbooking.module_3.dto.TripSearchResultDto;
import com.busbooking.module_3.service.RouteService;
import com.busbooking.module_3.service.TripSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Controller hiển thị danh mục tuyến đường nổi bật, bảng giờ chạy chi tiết theo tuyến và cổng /index (Module 3).
 */
@Controller
public class RouteController {

    private final RouteService routeService;
    private final TripSearchService tripSearchService;
    private final BusCompanyService busCompanyService;

    public RouteController(RouteService routeService,
                           TripSearchService tripSearchService,
                           BusCompanyService busCompanyService) {
        this.routeService = routeService;
        this.tripSearchService = tripSearchService;
        this.busCompanyService = busCompanyService;
    }

    /**
     * Trang cổng khám phá hành trình & tuyến đường liên tỉnh (/index).
     */
    @GetMapping("/index")
    public String indexHub(Model model) {
        List<RouteRecommendationDto> popularRoutes = routeService.getPopularRouteRecommendations();
        model.addAttribute("searchFilter", new TripSearchFilterDto());
        model.addAttribute("popularRoutes", popularRoutes);
        model.addAttribute("featuredTrips", tripSearchService.getFeaturedTrips());
        model.addAttribute("departureProvinces", tripSearchService.getAvailableDepartureProvinces());
        model.addAttribute("arrivalProvinces", tripSearchService.getAvailableArrivalProvinces());
        model.addAttribute("companies", busCompanyService.getActiveCompanies());
        return "module_3/index";
    }

    /**
     * Trang chi tiết và so sánh lịch trình theo tuyến xuất phát - điểm đến (/routes và /routes/detail).
     */
    @GetMapping({"/routes", "/routes/detail"})
    public String routeDetail(@RequestParam(value = "from", required = false) String fromProvince,
                              @RequestParam(value = "to", required = false) String toProvince,
                              Model model) {
        List<RouteRecommendationDto> popularRoutes = routeService.getPopularRouteRecommendations();
        RouteRecommendationDto selectedRoute = routeService.findRouteSummary(fromProvince, toProvince);
        List<TripSearchResultDto> routeTrips = routeService.getTripsForRoute(
                selectedRoute.getDepartureProvince(),
                selectedRoute.getArrivalProvince()
        );

        model.addAttribute("popularRoutes", popularRoutes);
        model.addAttribute("selectedRoute", selectedRoute);
        model.addAttribute("routeTrips", routeTrips);
        return "module_3/route-detail";
    }

    /**
     * API JSON trả về danh sách các tuyến đường phổ biến kèm mức giá khởi điểm.
     */
    @GetMapping("/api/routes/popular")
    @ResponseBody
    public ResponseEntity<List<RouteRecommendationDto>> popularRoutesApi() {
        return ResponseEntity.ok(routeService.getPopularRouteRecommendations());
    }
}
