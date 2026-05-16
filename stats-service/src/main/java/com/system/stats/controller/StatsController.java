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
            @RequestHeader("X-User-Id") Long masterId,
            @RequestParam(defaultValue = "TODAY") StatsPeriod period
    ) {
        return statsService.getMasterStats(masterId, period);
    }

    @GetMapping("/admin/master/{masterId}")
    public MasterStatsResponse getMasterStats(
            @PathVariable Long masterId,
            @RequestParam(defaultValue = "MONTH") StatsPeriod period
    ) {
        return statsService.getMasterStats(masterId, period);
    }

    @GetMapping("/admin/master/{masterId}/facts")
    public PageResponse<FinancialFactDTO> getMasterFacts(
            @PathVariable Long masterId,
            @RequestParam(defaultValue = "MONTH") StatsPeriod period,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return statsService.getMasterFacts(masterId, period, pageable);
    }
    
    @GetMapping("/admin/branch/{branchId}/masters")
    public PageResponse<MasterShortStatsDTO> getMastersByBranch(
            @PathVariable Long branchId,
            @RequestParam(defaultValue = "MONTH") StatsPeriod period,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return statsService.getMastersByBranch(branchId, period, pageable);
    }

    @GetMapping("/admin/branch/{branchId}/facts")
    public PageResponse<FinancialFactDTO> getBranchFacts(
            @PathVariable Long branchId,
            @RequestParam(defaultValue = "MONTH") StatsPeriod period,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return statsService.getBranchFacts(branchId, period, pageable);
    }

    @PostMapping("/admin/master/{masterId}/bonus")
    public Map<String, String> addBonus(
            @PathVariable Long masterId,
            @RequestHeader("X-Branch-Id") Long branchId,
            @RequestParam BigDecimal amount
    ) {
        statsService.addBonus(masterId, branchId, amount);
        return Map.of("status", "ok");
    }

    @PostMapping("/admin/master/{masterId}/penalty")
    public Map<String, String> addPenalty(
            @PathVariable Long masterId,
            @RequestHeader("X-Branch-Id") Long branchId,
            @RequestParam BigDecimal amount
    ) {
        statsService.addPenalty(masterId, branchId, amount);
        return Map.of("status", "ok");
    }
    
    @GetMapping("/admin/branches")
    public PageResponse<BranchAggregateDTO> getAllBranchesStats(
            @RequestParam(defaultValue = "MONTH") StatsPeriod period,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return statsService.getAllBranchesStats(period, pageable);
    }
}