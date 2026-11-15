package com.busbooking.module_4.repository;

import com.busbooking.module_4.enums.SeatStatus;
import com.busbooking.module_4.model.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository chuyên trách truy vấn và khóa bi quan danh sách ghế theo thứ tự ổn định (Module 4).
 */
@Repository
public interface SeatSelectionRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByBusTripIdOrderByFloorNumberAscRowIndexAscColIndexAsc(Long tripId);

    /**
     * Khóa ghi bi quan (PESSIMISTIC_WRITE) theo thứ tự ID tăng dần để ngăn deadlock và chống đặt trùng ghế.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id IN :seatIds AND s.busTrip.id = :tripId ORDER BY s.id ASC")
    List<Seat> findSeatsForHold(@Param("tripId") Long tripId, @Param("seatIds") List<Long> seatIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id IN :seatIds ORDER BY s.id ASC")
    List<Seat> findSeatsByIdsWithLock(@Param("seatIds") List<Long> seatIds);

    long countByBusTripIdAndStatus(Long tripId, SeatStatus status);
}
