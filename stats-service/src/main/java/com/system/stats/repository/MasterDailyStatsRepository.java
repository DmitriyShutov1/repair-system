package com.system.stats.repository;

import com.system.stats.dto.MasterAggregateDTO;
import com.system.stats.dto.MasterShortStatsDTO;
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
public interface MasterDailyStatsRepository extends JpaRepository<MasterDailyStats, Long> {
	
	Page<MasterDailyStats> findByBranchIdAndStatDateBetween(
		    Long branchId,
		    LocalDate start,
		    LocalDate end,
		    Pageable pageable
		);
	
	
	@Query("""
		    SELECT new com.system.stats.dto.MasterAggregateDTO(
		        COALESCE(SUM(m.orderCount), 0),
		        COALESCE(SUM(m.cancelledOrdersCount), 0),
		        COALESCE(SUM(m.returnedOrdersCount), 0),
		        COALESCE(SUM(m.totalIncome), 0)
		    )
		    FROM MasterDailyStats m
		    WHERE m.masterId = :masterId
		    AND m.statDate BETWEEN :startDate AND :endDate
		""")
		MasterAggregateDTO aggregateMasterStats(
		    @Param("masterId") Long masterId,
		    @Param("startDate") LocalDate startDate,
		    @Param("endDate") LocalDate endDate
		);
    
    Optional<MasterDailyStats> findByMasterIdAndStatDate(Long masterId, LocalDate statDate);
    
    List<MasterDailyStats> findByBranchId(Long branchId);
    
    List<MasterDailyStats> findByMasterIdAndStatDateBetween(Long masterId, LocalDate startDate, LocalDate endDate);
    
    List<MasterDailyStats> findByMasterIdAndStatDateAfter(Long masterId, LocalDate date);
    
    List<MasterDailyStats> findByMasterIdAndStatDateBefore(Long masterId, LocalDate date);
    
    List<MasterDailyStats> findByBranchIdAndStatDateBetween(Long branchId, LocalDate startDate, LocalDate endDate);
    
    List<MasterDailyStats> findByMasterIdAndOrderCountGreaterThan(Long masterId, Integer minOrderCount);
    
    @Query("SELECT SUM(m.totalIncome) FROM MasterDailyStats m WHERE m.masterId = :masterId AND m.statDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalIncomeByMasterAndDateRange(@Param("masterId") Long masterId, 
                                                   @Param("startDate") LocalDate startDate, 
                                                   @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(m.orderCount) FROM MasterDailyStats m WHERE m.masterId = :masterId AND m.statDate BETWEEN :startDate AND :endDate")
    Integer sumOrderCountByMasterAndDateRange(@Param("masterId") Long masterId, 
                                               @Param("startDate") LocalDate startDate, 
                                               @Param("endDate") LocalDate endDate);
    
    @Query("""
    		SELECT new com.system.stats.dto.MasterShortStatsDTO(
    		    m.masterId,
    		    m.branchId,
    		    SUM(m.orderCount),
    		    SUM(m.cancelledOrdersCount),
    		    SUM(m.returnedOrdersCount),
    		    SUM(m.totalIncome)
    		)
    		FROM MasterDailyStats m
    		WHERE m.branchId = :branchId
    		  AND m.statDate BETWEEN :startDate AND :endDate
    		GROUP BY m.masterId, m.branchId
    		ORDER BY SUM(m.totalIncome) DESC
    		""")
    		Page<MasterShortStatsDTO> findAggregatedByBranch(
    		        @Param("branchId") Long branchId,
    		        @Param("startDate") LocalDate startDate,
    		        @Param("endDate") LocalDate endDate,
    		        Pageable pageable
    		);
    
    boolean existsByMasterIdAndStatDate(Long masterId, LocalDate statDate);
}