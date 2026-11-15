package com.busbooking.module_1.repository;

import com.busbooking.common.enums.BusType;
import com.busbooking.common.enums.TripStatus;
import com.busbooking.module_1.model.BusTrip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusTripRepository extends JpaRepository<BusTrip, Long> {

    List<BusTrip> findAllByOrderByDepartureTimeDesc();

    List<BusTrip> findByStatusOrderByDepartureTimeAsc(TripStatus status);

    List<BusTrip> findByBusCompanyIdOrderByDepartureTimeDesc(Long companyId);

    List<BusTrip> findByLicensePlateIgnoreCaseAndStatusNot(String licensePlate, TripStatus excludedStatus);

    Optional<BusTrip> findByTripCodeIgnoreCase(String tripCode);

    long countByStatus(TripStatus status);

    long countByBusCompanyId(Long companyId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.busTrip.id = :tripId AND t.ticketStatus <> com.busbooking.common.enums.TicketStatus.CANCELLED")
    long countActiveTicketsByTripId(@Param("tripId") Long tripId);

    @Query("""
            SELECT COALESCE(SUM(t.availableSeats), 0)
            FROM BusTrip t
            WHERE t.status = :status
            """)
    long sumAvailableSeatsByStatus(@Param("status") TripStatus status);

    @Query("""
            SELECT t FROM BusTrip t
            WHERE (:keyword IS NULL OR :keyword = ''
                   OR LOWER(t.tripCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(t.routeName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(t.departureProvince) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(t.arrivalProvince) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(t.licensePlate) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(t.busCompany.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:companyId IS NULL OR t.busCompany.id = :companyId)
              AND (:status IS NULL OR t.status = :status)
              AND (:busType IS NULL OR t.busType = :busType)
              AND (:minPrice IS NULL OR t.ticketPrice >= :minPrice)
              AND (:maxPrice IS NULL OR t.ticketPrice <= :maxPrice)
              AND (:dayStart IS NULL OR t.departureTime >= :dayStart)
              AND (:dayEnd IS NULL OR t.departureTime < :dayEnd)
            """)
    Page<BusTrip> searchAdminTrips(@Param("keyword") String keyword,
                                   @Param("companyId") Long companyId,
                                   @Param("status") TripStatus status,
                                   @Param("busType") BusType busType,
                                   @Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice,
                                   @Param("dayStart") LocalDateTime dayStart,
                                   @Param("dayEnd") LocalDateTime dayEnd,
                                   Pageable pageable);
}
