package com.busbooking.module_5.repository;

import com.busbooking.module_5.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    List<PaymentTransaction> findByTicketIdOrderByCreatedAtDesc(Long ticketId);

    List<PaymentTransaction> findAllByOrderByCreatedAtDesc();
}
