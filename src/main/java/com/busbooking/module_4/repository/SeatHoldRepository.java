package com.busbooking.module_4.repository;

import com.busbooking.shared.model.SeatHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {

    List<SeatHold> findByHoldCodeAndActiveTrue(String holdCode);

    Optional<SeatHold> findBySeatIdAndActiveTrue(Long seatId);

    @Query("SELECT sh FROM SeatHold sh WHERE sh.active = true AND sh.expiresAt < :now")
    List<SeatHold> findExpiredActiveHolds(@Param("now") LocalDateTime now);

    @Query("SELECT sh FROM SeatHold sh WHERE sh.user.id = :userId AND sh.seat.busTrip.id = :tripId AND sh.active = true")
    List<SeatHold> findActiveHoldsByUserAndTrip(@Param("userId") Long userId, @Param("tripId") Long tripId);
}
