package com.system.stats.controller;

import com.system.stats.dto.BranchAggregateDTO;
import com.system.stats.dto.FinancialFactDTO;
import com.system.stats.dto.MasterShortStatsDTO;
import com.system.stats.dto.MasterStatsResponse;
import com.system.stats.dto.PageResponse;
import com.system.stats.dto.StatsPeriod;
import com.system.stats.entity.FinancialFact;
import com.system.stats.service.StatsQueryService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsQueryService statsService;

    @GetMapping("/master/me")
    public MasterStatsResponse getMyStats(
    		@RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") Long masterId,
            @RequestParam(defaultValue = "TODAY") StatsPeriod period
    ) {
    	if (!"MASTER".equals(role)) throw new IllegalStateException("Access denied");
        return statsService.getMasterStats(masterId, period);
    }

    @GetMapping("/admin/master/{masterId}")
    public MasterStatsResponse getMasterStats(
    		@RequestHeader("X-User-Role") String role,
            @PathVariable Long masterId,
            @RequestParam(defaultValue = "MONTH") StatsPeriod period
    ) {
    	if (!"ADMIN".equals(role)) throw new IllegalStateException("Access denied");
        return statsService.getMasterStats(masterId, period);
    }

    @GetMapping("/admin/master/{masterId}/facts")
    public PageResponse<FinancialFactDTO> getMasterFacts(
    		@RequestHeader("X-User-Role") String role,
            @PathVariable Long masterId,
            @RequestParam(defaultValue = "MONTH") StatsPeriod period,
            @PageableDefault(size = 20) Pageable pageable
    ) {
    	if (!"ADMIN".equals(role)) throw new IllegalStateException("Access denied");
        return statsService.getMasterFacts(masterId, period, pageable);
    }
    
    @GetMapping("/admin/branch/{branchId}/masters")
    public PageResponse<MasterShortStatsDTO> getMastersByBranch(
    		@RequestHeader("X-User-Role") String role,
            @PathVariable Long branchId,
            @RequestParam(defaultValue = "MONTH") StatsPeriod period,
            @PageableDefault(size = 20) Pageable pageable
    ) {
    	if (!"ADMIN".equals(role)) throw new IllegalStateException("Access denied");
        return statsService.getMastersByBranch(branchId, period, pageable);
    }

    @GetMapping("/admin/branch/{branchId}/facts")
    public PageResponse<FinancialFactDTO> getBranchFacts(
    		@RequestHeader("X-User-Role") String role,
            @PathVariable Long branchId,
            @RequestParam(defaultValue = "MONTH") StatsPeriod period,
            @PageableDefault(size = 20) Pageable pageable
    ) {
    	if (!"ADMIN".equals(role)) throw new IllegalStateException("Access denied");
        return statsService.getBranchFacts(branchId, period, pageable);
    }

    @PostMapping("/admin/master/{masterId}/bonus")
    public Map<String, String> addBonus(
    		@RequestHeader("X-User-Role") String role,
            @PathVariable Long masterId,
            @RequestHeader("X-Branch-Id") Long branchId,
            @RequestParam BigDecimal amount
    ) {
    	if (!"ADMIN".equals(role)) throw new IllegalStateException("Access denied");
        statsService.addBonus(masterId, branchId, amount);
        return Map.of("status", "ok");
    }

    @PostMapping("/admin/master/{masterId}/penalty")
    public Map<String, String> addPenalty(
    		@RequestHeader("X-User-Role") String role,
            @PathVariable Long masterId,
            @RequestHeader("X-Branch-Id") Long branchId,
            @RequestParam BigDecimal amount
    ) {
    	if (!"ADMIN".equals(role)) throw new IllegalStateException("Access denied");
        statsService.addPenalty(masterId, branchId, amount);
        return Map.of("status", "ok");
    }
    
    @GetMapping("/admin/branches")
    public PageResponse<BranchAggregateDTO> getAllBranchesStats(
    		@RequestHeader("X-User-Role") String role,
            @RequestParam(defaultValue = "MONTH") StatsPeriod period,
            @PageableDefault(size = 20) Pageable pageable
    ) {
    	if (!"ADMIN".equals(role)) throw new IllegalStateException("Access denied");
        return statsService.getAllBranchesStats(period, pageable);
    }
}