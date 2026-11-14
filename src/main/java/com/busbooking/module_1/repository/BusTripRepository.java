package com.busbooking.module_1.repository;

import com.busbooking.shared.enums.TripStatus;
import com.busbooking.shared.model.BusTrip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusTripRepository extends JpaRepository<BusTrip, Long> {

    List<BusTrip> findAllByOrderByDepartureTimeDesc();

    List<BusTrip> findByStatusOrderByDepartureTimeAsc(TripStatus status);

    List<BusTrip> findByBusCompanyIdOrderByDepartureTimeDesc(Long companyId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.busTrip.id = :tripId AND t.ticketStatus <> com.busbooking.shared.enums.TicketStatus.CANCELLED")
    long countActiveTicketsByTripId(@Param("tripId") Long tripId);
}
