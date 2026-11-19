package com.busbooking.module_6.repository;

import com.busbooking.module_4.enums.SeatStatus;
import com.busbooking.module_1.model.BusTrip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DashboardTripRepository extends JpaRepository<BusTrip, Long> {

    @Query("SELECT COALESCE(SUM(t.totalSeats), 0) FROM BusTrip t")
    long sumTotalSeatsCapacity();

    @Query("SELECT COUNT(s) FROM Seat s WHERE s.status = :status")
    long countSeatsByStatus(@Param("status") SeatStatus status);

    @Query("SELECT COUNT(s) FROM Seat s WHERE s.busTrip.id = :tripId AND s.status = :status")
    int countTripSeatsByStatus(@Param("tripId") Long tripId, @Param("status") SeatStatus status);

    @Query("SELECT DISTINCT t.routeName FROM BusTrip t ORDER BY t.routeName ASC")
    List<String> findAllDistinctRoutes();

    List<BusTrip> findAllByOrderByDepartureTimeDesc();
}
