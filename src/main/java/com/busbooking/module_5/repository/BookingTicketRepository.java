package com.busbooking.module_5.repository;

import com.busbooking.module_5.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingTicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Ticket> findAllByOrderByCreatedAtDesc();

    Optional<Ticket> findByTicketCode(String ticketCode);
}
