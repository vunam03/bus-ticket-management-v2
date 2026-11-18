package com.busbooking.module_3.repository;

import com.busbooking.common.enums.TripStatus;
import com.busbooking.module_1.model.BusTrip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripSearchRepository extends JpaRepository<BusTrip, Long> {

    List<BusTrip> findByStatusOrderByDepartureTimeAsc(TripStatus status);

    List<BusTrip> findByDepartureProvinceIgnoreCaseAndArrivalProvinceIgnoreCaseAndStatusOrderByDepartureTimeAsc(
            String departureProvince, String arrivalProvince, TripStatus status);

    @Query("SELECT DISTINCT t.departureProvince FROM BusTrip t ORDER BY t.departureProvince ASC")
    List<String> findDistinctDepartureProvinces();

    @Query("SELECT DISTINCT t.arrivalProvince FROM BusTrip t ORDER BY t.arrivalProvince ASC")
    List<String> findDistinctArrivalProvinces();

    @Query("SELECT t FROM BusTrip t WHERE t.status = :status ORDER BY t.departureTime ASC")
    List<BusTrip> findScheduledTrips(@Param("status") TripStatus status);

    long countByStatus(TripStatus status);
}
