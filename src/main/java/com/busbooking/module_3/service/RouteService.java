package com.busbooking.module_3.service;

import com.busbooking.common.enums.TripStatus;
import com.busbooking.module_1.model.BusTrip;
import com.busbooking.module_3.dto.RouteRecommendationDto;
import com.busbooking.module_3.dto.TripSearchResultDto;
import com.busbooking.module_3.repository.TripSearchRepository;
import com.busbooking.module_3.utils.CurrencyFormatter;
import com.busbooking.module_3.utils.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dịch vụ tổng hợp tuyến đường phổ biến, lịch trình tuyến và thông tin điểm đến (Module 3).
 */
@Service
public class RouteService {

    private final TripSearchRepository searchRepository;

    public RouteService(TripSearchRepository searchRepository) {
        this.searchRepository = searchRepository;
    }

    /**
     * Gom nhóm các chuyến xe mở bán thành danh sách tuyến đường đề xuất kèm giá vé thấp nhất.
     */
    @Transactional(readOnly = true)
    public List<RouteRecommendationDto> getPopularRouteRecommendations() {
        List<BusTrip> scheduled = searchRepository.findScheduledTrips(TripStatus.SCHEDULED).stream()
                .filter(trip -> trip.getBusCompany() != null && trip.getBusCompany().isActive())
                .collect(Collectors.toList());

        Map<String, List<BusTrip>> groupedByCorridor = new LinkedHashMap<>();
        for (BusTrip trip : scheduled) {
            String key = trip.getDepartureProvince() + " -> " + trip.getArrivalProvince();
            groupedByCorridor.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(trip);
        }

        return groupedByCorridor.values().stream()
                .map(this::buildRecommendation)
                .sorted(Comparator.comparingInt(RouteRecommendationDto::getActiveTripCount).reversed()
                        .thenComparing(RouteRecommendationDto::getMinPrice))
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách chuyến xe theo tuyến xuất phát - điểm đến cụ thể.
     */
    @Transactional(readOnly = true)
    public List<TripSearchResultDto> getTripsForRoute(String departureProvince, String arrivalProvince) {
        List<BusTrip> scheduled = searchRepository.findScheduledTrips(TripStatus.SCHEDULED);
        return scheduled.stream()
                .filter(trip -> trip.getBusCompany() != null && trip.getBusCompany().isActive())
                .filter(trip -> matchesIfPresent(trip.getDepartureProvince(), departureProvince))
                .filter(trip -> matchesIfPresent(trip.getArrivalProvince(), arrivalProvince))
                .map(TripSearchResultDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Trả về thống kê chi tiết cho một tuyến đường đã chọn (hoặc tuyến đầu tiên nếu không truyền tham số).
     */
    @Transactional(readOnly = true)
    public RouteRecommendationDto findRouteSummary(String departureProvince, String arrivalProvince) {
        List<RouteRecommendationDto> routes = getPopularRouteRecommendations();
        if (routes.isEmpty()) {
            return new RouteRecommendationDto(
                    "Đang cập nhật tuyến đường",
                    departureProvince != null ? departureProvince : "TP. Hồ Chí Minh",
                    arrivalProvince != null ? arrivalProvince : "Đà Lạt",
                    0,
                    0,
                    BigDecimal.ZERO,
                    CurrencyFormatter.formatVnd(BigDecimal.ZERO),
                    "Đang cập nhật",
                    "/images/destinations/default.svg",
                    "Tuyến liên tỉnh BusBookingVN"
            );
        }
        return routes.stream()
                .filter(r -> matchesIfPresent(r.getDepartureProvince(), departureProvince)
                        && matchesIfPresent(r.getArrivalProvince(), arrivalProvince))
                .findFirst()
                .orElse(routes.get(0));
    }

    private RouteRecommendationDto buildRecommendation(List<BusTrip> tripsInRoute) {
        BusTrip sample = tripsInRoute.get(0);
        BigDecimal minPrice = tripsInRoute.stream()
                .map(BusTrip::getTicketPrice)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        int totalSeats = tripsInRoute.stream()
                .mapToInt(t -> t.getAvailableSeats() != null ? t.getAvailableSeats() : 0)
                .sum();
        String routeName = sample.getDepartureProvince() + " → " + sample.getArrivalProvince();
        String duration = DateUtils.formatDuration(sample.getDepartureTime(), sample.getArrivalTime());
        String imageUrl = sample.getDestinationImageUrl();
        String highlight = sample.getDestinationHighlight();

        return new RouteRecommendationDto(
                routeName,
                sample.getDepartureProvince(),
                sample.getArrivalProvince(),
                tripsInRoute.size(),
                totalSeats,
                minPrice,
                CurrencyFormatter.formatVnd(minPrice),
                duration,
                imageUrl,
                highlight
        );
    }

    private boolean matchesIfPresent(String actual, String filterValue) {
        if (filterValue == null || filterValue.isBlank()) {
            return true;
        }
        return actual != null && actual.toLowerCase(Locale.ROOT)
                .contains(filterValue.trim().toLowerCase(Locale.ROOT));
    }
}
