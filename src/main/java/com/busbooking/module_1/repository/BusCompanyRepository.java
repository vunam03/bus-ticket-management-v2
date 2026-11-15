package com.busbooking.module_1.repository;

import com.busbooking.module_1.model.BusCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusCompanyRepository extends JpaRepository<BusCompany, Long> {

    List<BusCompany> findByActiveTrueOrderByNameAsc();

    List<BusCompany> findAllByOrderByCreatedAtDesc();

    Optional<BusCompany> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    long countByActiveTrue();

    @Query("""
            SELECT c FROM BusCompany c
            WHERE :keyword IS NULL OR :keyword = ''
               OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(c.hotline) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(COALESCE(c.headquarters, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY c.active DESC, c.rating DESC, c.createdAt DESC
            """)
    List<BusCompany> searchCompanies(@Param("keyword") String keyword);
}
