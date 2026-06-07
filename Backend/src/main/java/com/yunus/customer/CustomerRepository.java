package com.yunus.customer;

import com.yunus.dashboard.dto.TagCustomerCountResponse;
import com.yunus.enums.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID>, JpaSpecificationExecutor<Customer> {

    Optional<Customer> findById(UUID id);

    Page<Customer> findAll(Pageable pageable);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);

    long count();

    long countByStatus(CustomerStatus status);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("""
            SELECT new com.yunus.dashboard.dto.TagCustomerCountResponse(
                t.id,
                t.name,
                COUNT(c.id)
            )
            FROM Customer c
            JOIN c.tags t
            WHERE c.isDeleted = false
              AND t.isDeleted = false
            GROUP BY t.id, t.name
            ORDER BY COUNT(c.id) DESC
            """)

List<TagCustomerCountResponse> countCustomersByTag();
}
