package com.busbooking.module_6.repository;

import com.busbooking.common.enums.PaymentStatus;
import com.busbooking.common.enums.TicketStatus;
import com.busbooking.module_5.model.Ticket;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DashboardTicketRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT COALESCE(SUM(t.totalAmount), 0) FROM Ticket t WHERE t.ticketStatus = :ticketStatus AND t.paymentStatus = :paymentStatus")
    BigDecimal sumConfirmedPaidRevenue(@Param("ticketStatus") TicketStatus ticketStatus,
                                       @Param("paymentStatus") PaymentStatus paymentStatus);

    long countByTicketStatusAndPaymentStatus(TicketStatus ticketStatus, PaymentStatus paymentStatus);

    long countByTicketStatus(TicketStatus ticketStatus);

    long countByPaymentStatus(PaymentStatus paymentStatus);

    @Query("SELECT t FROM Ticket t WHERE t.ticketStatus = :ticketStatus AND t.paymentStatus = :paymentStatus AND t.createdAt BETWEEN :start AND :end ORDER BY t.createdAt ASC")
    List<Ticket> findPaidTicketsBetween(@Param("ticketStatus") TicketStatus ticketStatus,
                                        @Param("paymentStatus") PaymentStatus paymentStatus,
                                        @Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);

    @Query("SELECT t FROM Ticket t WHERE t.createdAt BETWEEN :start AND :end ORDER BY t.createdAt ASC")
    List<Ticket> findAllTicketsBetween(@Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    @Query("SELECT t.busTrip.busCompany.name, t.busTrip.busCompany.hotline, COUNT(t), COALESCE(SUM(t.totalAmount), 0) " +
           "FROM Ticket t WHERE t.ticketStatus = :ticketStatus AND t.paymentStatus = :paymentStatus " +
           "GROUP BY t.busTrip.busCompany.id, t.busTrip.busCompany.name, t.busTrip.busCompany.hotline " +
           "ORDER BY SUM(t.totalAmount) DESC")
    List<Object[]> aggregateSalesByCompany(@Param("ticketStatus") TicketStatus ticketStatus,
                                           @Param("paymentStatus") PaymentStatus paymentStatus);

    @Query("SELECT t.busTrip.routeName, t.busTrip.departureStation, COUNT(t), COALESCE(SUM(t.totalAmount), 0) " +
           "FROM Ticket t WHERE t.ticketStatus = :ticketStatus AND t.paymentStatus = :paymentStatus " +
           "GROUP BY t.busTrip.routeName, t.busTrip.departureStation " +
           "ORDER BY SUM(t.totalAmount) DESC")
    List<Object[]> aggregateSalesByRoute(@Param("ticketStatus") TicketStatus ticketStatus,
                                         @Param("paymentStatus") PaymentStatus paymentStatus);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.user.id = :userId")
    long countTotalTicketsByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.user.id = :userId AND t.ticketStatus = :ticketStatus AND t.paymentStatus = :paymentStatus")
    long countPaidTicketsByUser(@Param("userId") Long userId,
                                @Param("ticketStatus") TicketStatus ticketStatus,
                                @Param("paymentStatus") PaymentStatus paymentStatus);

    @Query("SELECT COALESCE(SUM(t.totalAmount), 0) FROM Ticket t WHERE t.user.id = :userId AND t.ticketStatus = :ticketStatus AND t.paymentStatus = :paymentStatus")
    BigDecimal sumTotalSpentByUser(@Param("userId") Long userId,
                                   @Param("ticketStatus") TicketStatus ticketStatus,
                                   @Param("paymentStatus") PaymentStatus paymentStatus);

    @Query("SELECT t FROM Ticket t ORDER BY t.createdAt DESC")
    List<Ticket> findRecentTickets(Pageable pageable);
}
