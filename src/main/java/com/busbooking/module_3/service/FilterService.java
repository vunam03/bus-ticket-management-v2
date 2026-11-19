package com.busbooking.module_3.service;

import com.busbooking.common.enums.BusType;
import com.busbooking.module_1.model.BusTrip;
import com.busbooking.module_3.dto.TripSearchFilterDto;
import com.busbooking.module_3.utils.CurrencyFormatter;
import com.busbooking.module_3.utils.DateUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dịch vụ lọc, phân loại khung giờ và sắp xếp danh sách chuyến xe theo nhiều tiêu chí (Module 3).
 */
@Service
public class FilterService {

    private static final List<String> TIME_SLOTS = List.of("MORNING", "AFTERNOON", "EVENING", "NIGHT");

    /**
     * Lọc và sắp xếp danh sách chuyến xe ứng viên theo bộ lọc của người dùng.
     */
    public List<BusTrip> filterAndSortTrips(List<BusTrip> candidates, TripSearchFilterDto filter) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        TripSearchFilterDto activeFilter = filter != null ? filter : new TripSearchFilterDto();
        return candidates.stream()
                .filter(trip -> trip.getBusCompany() != null && trip.getBusCompany().isActive())
                .filter(trip -> matchesProvince(trip.getDepartureProvince(), activeFilter.getDepartureProvince()))
                .filter(trip -> matchesProvince(trip.getArrivalProvince(), activeFilter.getArrivalProvince()))
                .filter(trip -> activeFilter.getDepartureDate() == null
                        || (trip.getDepartureTime() != null
                        && trip.getDepartureTime().toLocalDate().equals(activeFilter.getDepartureDate())))
                .filter(trip -> activeFilter.getMinPrice() == null
                        || (trip.getTicketPrice() != null
                        && trip.getTicketPrice().compareTo(activeFilter.getMinPrice()) >= 0))
                .filter(trip -> activeFilter.getMaxPrice() == null
                        || (trip.getTicketPrice() != null
                        && trip.getTicketPrice().compareTo(activeFilter.getMaxPrice()) <= 0))
                .filter(trip -> activeFilter.getBusType() == null || trip.getBusType() == activeFilter.getBusType())
                .filter(trip -> activeFilter.getCompanyId() == null
                        || trip.getBusCompany().getId().equals(activeFilter.getCompanyId()))
                .filter(trip -> matchesTimeSlot(trip, activeFilter.getTimeSlot()))
                .sorted(buildComparator(activeFilter.getSortBy()))
                .collect(Collectors.toList());
    }

    /**
     * Đếm số lượng chuyến xe theo từng khung giờ khởi hành.
     */
    public Map<String, Long> countByTimeSlot(List<BusTrip> trips) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String slot : TIME_SLOTS) {
            counts.put(slot, 0L);
        }
        if (trips == null) {
            return counts;
        }
        for (BusTrip trip : trips) {
            if (trip.getDepartureTime() != null) {
                String slot = DateUtils.classifyTimeSlotCode(trip.getDepartureTime());
                counts.computeIfPresent(slot, (key, val) -> val + 1L);
            }
        }
        return counts;
    }

    /**
     * Thống kê số lượng chuyến xe theo từng dòng xe.
     */
    public Map<String, Long> countByBusType(List<BusTrip> trips) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (BusType type : BusType.values()) {
            counts.put(type.name(), 0L);
        }
        if (trips == null) {
            return counts;
        }
        for (BusTrip trip : trips) {
            if (trip.getBusType() != null) {
                counts.computeIfPresent(trip.getBusType().name(), (key, val) -> val + 1L);
            }
        }
        return counts;
    }

    /**
     * Tính toán tóm tắt khoảng giá (thấp nhất, cao nhất, trung bình) của danh sách chuyến xe.
     */
    public Map<String, Object> calculatePriceSummary(List<BusTrip> trips) {
        Map<String, Object> summary = new LinkedHashMap<>();
        if (trips == null || trips.isEmpty()) {
            summary.put("count", 0);
            summary.put("minFormatted", "0 đ");
            summary.put("maxFormatted", "0 đ");
            summary.put("avgFormatted", "0 đ");
            return summary;
        }

        BigDecimal min = null;
        BigDecimal max = null;
        BigDecimal total = BigDecimal.ZERO;
        int validCount = 0;

        for (BusTrip trip : trips) {
            BigDecimal price = trip.getTicketPrice();
            if (price == null) {
                continue;
            }
            if (min == null || price.compareTo(min) < 0) {
                min = price;
            }
            if (max == null || price.compareTo(max) > 0) {
                max = price;
            }
            total = total.add(price);
            validCount++;
        }

        BigDecimal average = validCount > 0
                ? total.divide(BigDecimal.valueOf(validCount), 0, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        summary.put("count", trips.size());
        summary.put("minPrice", min != null ? min : BigDecimal.ZERO);
        summary.put("maxPrice", max != null ? max : BigDecimal.ZERO);
        summary.put("minFormatted", CurrencyFormatter.formatVnd(min));
        summary.put("maxFormatted", CurrencyFormatter.formatVnd(max));
        summary.put("avgFormatted", CurrencyFormatter.formatVnd(average));
        return summary;
    }

    private boolean matchesProvince(String actualProvince, String requestedProvince) {
        if (requestedProvince == null || requestedProvince.isBlank()) {
            return true;
        }
        if (actualProvince == null) {
            return false;
        }
        return actualProvince.toLowerCase(Locale.ROOT)
                .contains(requestedProvince.trim().toLowerCase(Locale.ROOT));
    }

    private boolean matchesTimeSlot(BusTrip trip, String timeSlot) {
        if (timeSlot == null || timeSlot.isBlank()) {
            return true;
        }
        if (trip.getDepartureTime() == null) {
            return false;
        }
        String classified = DateUtils.classifyTimeSlotCode(trip.getDepartureTime());
        return classified.equalsIgnoreCase(timeSlot.trim());
    }

    private Comparator<BusTrip> buildComparator(String sortBy) {
        if (sortBy == null) {
            return Comparator.comparing(BusTrip::getDepartureTime);
        }
        return switch (sortBy.trim().toUpperCase(Locale.ROOT)) {
            case "PRICE_ASC" -> Comparator.comparing(BusTrip::getTicketPrice)
                    .thenComparing(BusTrip::getDepartureTime);
            case "PRICE_DESC" -> Comparator.comparing(BusTrip::getTicketPrice).reversed()
                    .thenComparing(BusTrip::getDepartureTime);
            case "SEATS_DESC" -> Comparator.comparing(BusTrip::getAvailableSeats).reversed()
                    .thenComparing(BusTrip::getDepartureTime);
            case "TIME_DESC" -> Comparator.comparing(BusTrip::getDepartureTime).reversed();
            default -> Comparator.comparing(BusTrip::getDepartureTime);
        };
    }
}
