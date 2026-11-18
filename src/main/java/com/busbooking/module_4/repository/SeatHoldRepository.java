package com.busbooking.module_4.repository;

import com.busbooking.module_4.model.SeatHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý phiên giữ ghế tạm thời (Module 4).
 */
@Repository
public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {

    List<SeatHold> findByHoldCodeAndActiveTrue(String holdCode);

    List<SeatHold> findByHoldCodeOrderByIdAsc(String holdCode);

    Optional<SeatHold> findBySeatIdAndActiveTrue(Long seatId);

    List<SeatHold> findAllBySeatIdAndActiveTrueOrderByIdAsc(Long seatId);

    @Query("SELECT sh FROM SeatHold sh WHERE sh.active = true AND sh.expiresAt < :now ORDER BY sh.seat.id ASC")
    List<SeatHold> findExpiredActiveHolds(@Param("now") LocalDateTime now);

    @Query("SELECT sh FROM SeatHold sh WHERE sh.seat.busTrip.id = :tripId AND sh.active = true ORDER BY sh.seat.id ASC")
    List<SeatHold> findActiveHoldsByTrip(@Param("tripId") Long tripId);

    @Query("SELECT sh FROM SeatHold sh WHERE sh.user.id = :userId AND sh.seat.busTrip.id = :tripId AND sh.active = true ORDER BY sh.seat.id ASC")
    List<SeatHold> findActiveHoldsByUserAndTrip(@Param("userId") Long userId, @Param("tripId") Long tripId);

    @Query("SELECT COUNT(sh) > 0 FROM SeatHold sh WHERE sh.seat.id = :seatId AND sh.active = true AND sh.expiresAt >= :now")
    boolean existsActiveNonExpiredHoldForSeat(@Param("seatId") Long seatId, @Param("now") LocalDateTime now);
}
