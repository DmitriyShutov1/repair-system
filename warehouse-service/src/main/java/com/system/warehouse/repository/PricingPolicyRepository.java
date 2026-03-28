package com.system.warehouse.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.system.warehouse.entity.*;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
public interface PricingPolicyRepository extends JpaRepository<PricingPolicy, Long> {

    /* =======================
       SEARCH BY PART
       ======================= */

    Page<PricingPolicy> findAllByPartId(Long partId, Pageable pageable);

    /* =======================
       SEARCH BY SERVICE
       ======================= */

    Page<PricingPolicy> findAllByServiceId(Long serviceId, Pageable pageable);

    /* =======================
       CURRENT ACTIVE POLICY
       ======================= */

    @Query("""
           SELECT p
           FROM PricingPolicy p
           WHERE p.part.id = :partId
             AND p.effectiveTo IS NULL
           """)
    Optional<PricingPolicy> findActiveByPartId(@Param("partId") Long partId);

    @Query("""
           SELECT p
           FROM PricingPolicy p
           WHERE p.service.id = :serviceId
             AND p.effectiveTo IS NULL
           """)
    Optional<PricingPolicy> findActiveByServiceId(@Param("serviceId") Long serviceId);

    /* =======================
       HISTORY LOOKUP
       ======================= */

    @Query("""
           SELECT p
           FROM PricingPolicy p
           WHERE p.part.id = :partId
             AND :moment BETWEEN p.effectiveFrom
                             AND COALESCE(p.effectiveTo, :moment)
           """)
    Optional<PricingPolicy> findActualAtMoment(
            @Param("partId") Long partId,
            @Param("moment") LocalDateTime moment
    );
}