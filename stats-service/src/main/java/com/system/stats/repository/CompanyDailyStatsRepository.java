package com.system.stats.repository;

import com.system.stats.dto.BranchAggregateDTO;
import com.system.stats.entity.CompanyDailyStats;
import com.system.stats.entity.FinancialFact;
import com.system.stats.entity.FinancialFact.OperationType;
import com.system.stats.entity.MasterDailyStats;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Repository
public interface CompanyDailyStatsRepository extends JpaRepository<CompanyDailyStats, Long> {
    
    Optional<CompanyDailyStats> findByStatDateAndBranchId(LocalDate statDate, Long branchId);
    
    List<CompanyDailyStats> findByBranchIdAndStatDateBetween(Long branchId, LocalDate startDate, LocalDate endDate);
    
    List<CompanyDailyStats> findByStatDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<CompanyDailyStats> findByBranchIdAndStatDateAfter(Long branchId, LocalDate date);
    
    List<CompanyDailyStats> findByBranchIdAndStatDateBefore(Long branchId, LocalDate date);
    
    List<CompanyDailyStats> findByBranchIdAndTotalOrdersGreaterThan(Long branchId, Integer minOrders);
    
    List<CompanyDailyStats> findByBranchId(Long branchId);
    
    List<CompanyDailyStats> findByStatDate(LocalDate statDate);
    
    @Query("SELECT COALESCE(SUM(c.totalIncome), 0) FROM CompanyDailyStats c " +
           "WHERE c.branchId = :branchId AND c.statDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalIncomeByBranchAndDateRange(@Param("branchId") Long branchId,
                                                   @Param("startDate") LocalDate startDate, 
                                                   @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COALESCE(SUM(c.totalIncome), 0) FROM CompanyDailyStats c " +
           "WHERE c.statDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalIncomeByDateRange(@Param("startDate") LocalDate startDate, 
                                         @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COALESCE(SUM(c.totalExpenses), 0) FROM CompanyDailyStats c " +
           "WHERE c.branchId = :branchId AND c.statDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalExpensesByBranchAndDateRange(@Param("branchId") Long branchId,
                                                     @Param("startDate") LocalDate startDate, 
                                                     @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COALESCE(SUM(c.totalExpenses), 0) FROM CompanyDailyStats c " +
           "WHERE c.statDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalExpensesByDateRange(@Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COALESCE(SUM(c.totalOrders), 0) FROM CompanyDailyStats c " +
           "WHERE c.branchId = :branchId AND c.statDate BETWEEN :startDate AND :endDate")
    Integer sumTotalOrdersByBranchAndDateRange(@Param("branchId") Long branchId,
                                                @Param("startDate") LocalDate startDate, 
                                                @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COALESCE(SUM(c.totalOrders), 0) FROM CompanyDailyStats c " +
           "WHERE c.statDate BETWEEN :startDate AND :endDate")
    Integer sumTotalOrdersByDateRange(@Param("startDate") LocalDate startDate, 
                                      @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COALESCE(SUM(c.totalReturns), 0) FROM CompanyDailyStats c " +
           "WHERE c.branchId = :branchId AND c.statDate BETWEEN :startDate AND :endDate")
    Integer sumTotalReturnsByBranchAndDateRange(@Param("branchId") Long branchId,
                                                 @Param("startDate") LocalDate startDate, 
                                                 @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COALESCE(SUM(c.totalReturns), 0) FROM CompanyDailyStats c " +
           "WHERE c.statDate BETWEEN :startDate AND :endDate")
    Integer sumTotalReturnsByDateRange(@Param("startDate") LocalDate startDate, 
                                       @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COALESCE(SUM(c.totalIncome), 0) - COALESCE(SUM(c.totalExpenses), 0) " +
           "FROM CompanyDailyStats c WHERE c.branchId = :branchId AND c.statDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateNetProfitByBranchAndDateRange(@Param("branchId") Long branchId,
                                                       @Param("startDate") LocalDate startDate, 
                                                       @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COALESCE(SUM(c.totalIncome), 0) - COALESCE(SUM(c.totalExpenses), 0) " +
           "FROM CompanyDailyStats c WHERE c.statDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateNetProfitByDateRange(@Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate);
    
    boolean existsByStatDateAndBranchId(LocalDate statDate, Long branchId);
    
    Optional<CompanyDailyStats> findFirstByBranchIdOrderByStatDateDesc(Long branchId);
    
    Optional<CompanyDailyStats> findFirstByBranchIdOrderByStatDateAsc(Long branchId);
    
    @Query("SELECT c.statDate FROM CompanyDailyStats c WHERE c.branchId = :branchId ORDER BY c.statDate")
    List<LocalDate> findAllStatDatesByBranchId(@Param("branchId") Long branchId);
    

    @Query("""
    	    SELECT new com.system.stats.dto.BranchAggregateDTO(
    	        c.branchId,
    	        COALESCE(SUM(c.totalIncome), 0),
    	        COALESCE(SUM(c.totalExpenses), 0),
    	        COALESCE(SUM(c.totalOrders), 0),
    	        COALESCE(SUM(c.totalReturns), 0)
    	    )
    	    FROM CompanyDailyStats c
    	    WHERE c.statDate BETWEEN :startDate AND :endDate
    	    GROUP BY c.branchId
    	    ORDER BY SUM(c.totalIncome) DESC
    	""")
    	Page<BranchAggregateDTO> aggregateBranches(
    	    @Param("startDate") LocalDate start,
    	    @Param("endDate") LocalDate end,
    	    Pageable pageable
    	);
}