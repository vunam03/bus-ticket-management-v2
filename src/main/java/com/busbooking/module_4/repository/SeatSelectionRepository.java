package com.busbooking.module_4.repository;

import com.busbooking.shared.enums.SeatStatus;
import com.busbooking.shared.model.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatSelectionRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByBusTripIdOrderByFloorNumberAscRowIndexAscColIndexAsc(Long tripId);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT s FROM Seat s WHERE s.id IN :seatIds AND s.busTrip.id = :tripId")
    List<Seat> findSeatsForHold(@Param("tripId") Long tripId, @Param("seatIds") List<Long> seatIds);

    long countByBusTripIdAndStatus(Long tripId, SeatStatus status);
}
