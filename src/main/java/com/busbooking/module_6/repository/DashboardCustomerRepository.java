package com.busbooking.module_6.repository;

import com.busbooking.common.enums.RoleName;
import com.busbooking.module_2.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DashboardCustomerRepository extends JpaRepository<User, Long> {

    @Query("SELECT COUNT(u) FROM User u WHERE u.role.name = :roleName")
    long countByRoleName(@Param("roleName") RoleName roleName);

    @Query("SELECT u FROM User u WHERE u.role.name = :roleName AND (" +
           ":keyword IS NULL OR :keyword = '' OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(COALESCE(u.phone, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY u.createdAt DESC")
    Page<User> searchCustomers(@Param("roleName") RoleName roleName,
                               @Param("keyword") String keyword,
                               Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role.name = :roleName ORDER BY u.createdAt DESC")
    List<User> findAllCustomers(@Param("roleName") RoleName roleName);
}
