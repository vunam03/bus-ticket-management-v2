package com.busbooking.module_4.repository;

import com.busbooking.module_4.enums.SeatStatus;
import com.busbooking.module_4.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository chuyên trách truy vấn sơ đồ ghế theo tầng và trạng thái cho Module 4.
 */
@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByBusTripIdOrderByFloorNumberAscRowIndexAscColIndexAsc(Long tripId);

    List<Seat> findByBusTripIdAndFloorNumberOrderByRowIndexAscColIndexAsc(Long tripId, Integer floorNumber);

    List<Seat> findByBusTripIdAndStatusOrderByFloorNumberAscRowIndexAscColIndexAsc(Long tripId, SeatStatus status);

    Optional<Seat> findByBusTripIdAndSeatNumber(Long tripId, String seatNumber);

    long countByBusTripIdAndStatus(Long tripId, SeatStatus status);

    long countByBusTripIdAndFloorNumberAndStatus(Long tripId, Integer floorNumber, SeatStatus status);
}
