package com.busbooking.module_5.repository;

import com.busbooking.common.enums.TicketStatus;
import com.busbooking.module_5.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository bổ sung phục vụ tra cứu vé theo mã vé không phân biệt hoa thường và lọc trạng thái vé (Module 5).
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketCodeIgnoreCase(String ticketCode);

    List<Ticket> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Ticket> findByUserIdAndTicketStatusOrderByCreatedAtDesc(Long userId, TicketStatus ticketStatus);

    long countByUserIdAndTicketStatus(Long userId, TicketStatus ticketStatus);
}
