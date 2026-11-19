package com.busbooking.module_3.service;

import com.busbooking.common.enums.TripStatus;
import com.busbooking.common.exception.ResourceNotFoundException;
import com.busbooking.module_1.model.BusTrip;
import com.busbooking.module_3.dto.TripSearchFilterDto;
import com.busbooking.module_3.repository.TripSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Dịch vụ tìm kiếm chuyến xe cốt lõi và cung cấp danh mục tỉnh/thành mở bán (Module 3).
 */
@Service
public class TripSearchService {

    private final TripSearchRepository searchRepository;
    private final FilterService filterService;

    public TripSearchService(TripSearchRepository searchRepository) {
        this(searchRepository, new FilterService());
    }

    @Autowired
    public TripSearchService(TripSearchRepository searchRepository, FilterService filterService) {
        this.searchRepository = searchRepository;
        this.filterService = filterService;
    }

    @Transactional(readOnly = true)
    public List<String> getAvailableDepartureProvinces() {
        return searchRepository.findDistinctDepartureProvinces();
    }

    @Transactional(readOnly = true)
    public List<String> getAvailableArrivalProvinces() {
        return searchRepository.findDistinctArrivalProvinces();
    }

    @Transactional(readOnly = true)
    public List<BusTrip> getFeaturedTrips() {
        return searchRepository.findScheduledTrips(TripStatus.SCHEDULED).stream()
                .filter(t -> t.getBusCompany() != null && t.getBusCompany().isActive())
                .limit(8)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BusTrip getTripDetail(Long id) {
        return searchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến xe với mã ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<BusTrip> searchAndFilterTrips(TripSearchFilterDto filter) {
        List<BusTrip> allScheduled = searchRepository.findScheduledTrips(TripStatus.SCHEDULED);
        return filterService.filterAndSortTrips(allScheduled, filter);
    }
}
