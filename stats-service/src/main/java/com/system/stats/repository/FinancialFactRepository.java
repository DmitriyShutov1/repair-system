package com.system.stats.repository;

import com.system.stats.entity.FinancialFact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.system.stats.entity.FinancialFact.OperationType;
import com.system.stats.entity.MasterDailyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FinancialFactRepository extends JpaRepository<FinancialFact, Long> {
    
    List<FinancialFact> findByMasterIdAndEventDate(Long masterId, LocalDate eventDate);
    
    boolean existsByEventId(UUID eventId);
    
    Page<FinancialFact> findByMasterIdAndEventDateBetweenOrderByEventDateDesc(
            Long masterId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    Page<FinancialFact> findByBranchIdAndEventDateBetweenOrderByEventDateDesc(
            Long branchId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );
    
    List<FinancialFact> findByMasterIdAndEventDateBetween(Long masterId, LocalDate startDate, LocalDate endDate);
    
    List<FinancialFact> findByBranchIdAndEventDateBetween(Long branchId, LocalDate startDate, LocalDate endDate);
    
    List<FinancialFact> findByType(OperationType type);
    
    List<FinancialFact> findByOrderId(Long orderId);
    
    List<FinancialFact> findByMasterIdAndTypeAndEventDateBetween(Long masterId, 
                                                                  OperationType type, 
                                                                  LocalDate startDate, 
                                                                  LocalDate endDate);
    
    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FinancialFact f " +
           "WHERE f.masterId = :masterId " +
           "AND f.eventDate BETWEEN :startDate AND :endDate " +
           "AND f.type IN :types")
    BigDecimal sumAmountByMasterAndTypesAndDateRange(@Param("masterId") Long masterId,
                                                      @Param("types") List<OperationType> types,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);
    
    @Query("SELECT f.type, COALESCE(SUM(f.amount), 0) FROM FinancialFact f " +
           "WHERE f.masterId = :masterId " +
           "AND f.eventDate BETWEEN :startDate AND :endDate " +
           "GROUP BY f.type")
    List<Object[]> getAmountGroupedByType(@Param("masterId") Long masterId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);
    
    List<FinancialFact> findByOrderIdOrderByEventDateAsc(Long orderId);
    
    org.springframework.data.domain.Page<FinancialFact> findByMasterId(Long masterId, 
                                                                        org.springframework.data.domain.Pageable pageable);
    
    boolean existsByMasterIdAndEventDate(Long masterId, LocalDate eventDate);
}