package com.busbooking.module_1.repository;

import com.busbooking.shared.enums.SeatStatus;
import com.busbooking.shared.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripSeatGeneratorRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByBusTripIdOrderByFloorNumberAscRowIndexAscColIndexAsc(Long tripId);

    long countByBusTripIdAndStatus(Long tripId, SeatStatus status);

    void deleteByBusTripId(Long tripId);
}
