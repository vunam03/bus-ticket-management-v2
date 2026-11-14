package com.busbooking.module_1.repository;

import com.busbooking.shared.model.BusCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusCompanyRepository extends JpaRepository<BusCompany, Long> {

    List<BusCompany> findByActiveTrueOrderByNameAsc();

    List<BusCompany> findAllByOrderByCreatedAtDesc();

    Optional<BusCompany> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
