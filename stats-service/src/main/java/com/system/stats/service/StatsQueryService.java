package com.system.stats.service;

import com.system.stats.dto.BranchAggregateDTO;
import com.system.stats.dto.FinancialFactDTO;
import com.system.stats.dto.MasterAggregateDTO;
import com.system.stats.dto.MasterShortStatsDTO;
import com.system.stats.dto.MasterStatsResponse;
import com.system.stats.dto.PageResponse;
import com.system.stats.dto.StatsPeriod;
import com.system.stats.entity.CompanyDailyStats;
import com.system.stats.entity.FinancialFact;
import com.system.stats.entity.FinancialFact.OperationType;
import com.system.stats.entity.MasterDailyStats;
import com.system.stats.repository.CompanyDailyStatsRepository;
import com.system.stats.repository.FinancialFactRepository;
import com.system.stats.repository.MasterDailyStatsRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsQueryService {

    private final MasterDailyStatsRepository masterRepo;
    private final CompanyDailyStatsRepository companyRepo;
    private final FinancialFactRepository factRepo;

    public MasterStatsResponse getMasterStats(Long masterId, StatsPeriod period) {

        DateRange range = resolve(period);

        MasterAggregateDTO result = masterRepo.aggregateMasterStats(
                masterId,
                range.start,
                range.end
        );

        return MasterStatsResponse.builder()
                .masterId(masterId)
                .period(period.name())
                .totalOrders(result.getTotalOrders())
                .cancelledOrders(result.getCancelledOrders())
                .returnedOrders(result.getReturnedOrders())
                .totalIncome(result.getTotalIncome())
                .build();
    }

    public PageResponse<FinancialFactDTO> getMasterFacts(
            Long masterId,
            StatsPeriod period,
            Pageable pageable
    ) {
        DateRange range = resolve(period);

        Page<FinancialFact> page =
                factRepo.findByMasterIdAndEventDateBetweenOrderByEventDateDesc(masterId, range.start, range.end, pageable);

        return toPage(page);
    }

    public PageResponse<FinancialFactDTO> getBranchFacts(
            Long branchId,
            StatsPeriod period,
            Pageable pageable
    ) {
        DateRange range = resolve(period);

        Page<FinancialFact> page =
                factRepo.findByBranchIdAndEventDateBetweenOrderByEventDateDesc(branchId, range.start, range.end, pageable);

        return toPage(page);
    }

    public PageResponse<MasterShortStatsDTO> getMastersByBranch(
            Long branchId,
            StatsPeriod period,
            Pageable pageable
    ) {

        DateRange range = resolve(period);

        Page<MasterShortStatsDTO> page =
                masterRepo.findAggregatedByBranch(
                        branchId,
                        range.start,
                        range.end,
                        pageable
                );

        return PageResponse.<MasterShortStatsDTO>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }


    @Transactional
    public void addBonus(Long masterId, Long branchId, BigDecimal amount) {
    	
    	Long branch = factRepo.findFirstByMasterIdOrderByEventDateDesc(masterId)
                .map(FinancialFact::getBranchId)
                .orElseThrow(() -> new IllegalStateException("No facts found for master"));
    	
        saveFact(masterId, branch, amount, OperationType.BONUS);
        LocalDate today = LocalDate.now();
        MasterDailyStats master = masterRepo
                .findByMasterIdAndStatDate(masterId, today)
                .orElse(MasterDailyStats.builder()
                        .masterId(masterId)
                        .branchId(branch)
                        .statDate(today)
                        .build());
        master.setTotalIncome(master.getTotalIncome().add(amount));
        masterRepo.save(master);
        
        CompanyDailyStats company = companyRepo
                .findByStatDateAndBranchId(today, branch)
                .orElse(CompanyDailyStats.builder()
                        .branchId(branch)
                        .statDate(today)
                        .build());
        company.setTotalExpenses(company.getTotalExpenses().add(amount));
        companyRepo.save(company);
    }

    @Transactional
    public void addPenalty(Long masterId, Long branchId, BigDecimal amount) {
    	Long branch = factRepo.findFirstByMasterIdOrderByEventDateDesc(masterId)
                .map(FinancialFact::getBranchId)
                .orElseThrow(() -> new IllegalStateException("No facts found for master"));
    	
        saveFact(masterId, branch, amount, OperationType.PENALTY);
        LocalDate today = LocalDate.now();
        MasterDailyStats master = masterRepo
                .findByMasterIdAndStatDate(masterId, today)
                .orElse(MasterDailyStats.builder()
                        .masterId(masterId)
                        .branchId(branch)
                        .statDate(today)
                        .build());
        master.setTotalIncome(master.getTotalIncome().subtract(amount));
        masterRepo.save(master);
        
        CompanyDailyStats company = companyRepo
                .findByStatDateAndBranchId(today, branch)
                .orElse(CompanyDailyStats.builder()
                        .branchId(branch)
                        .statDate(today)
                        .build());
        company.setTotalIncome(company.getTotalIncome().add(amount));
        companyRepo.save(company);
        
    }

    private void saveFact(Long masterId, Long branchId, BigDecimal amount, OperationType type) {

        FinancialFact fact = FinancialFact.builder()
        		.eventId(UUID.randomUUID())
                .masterId(masterId)
                .branchId(branchId)
                .type(type)
                .amount(amount)
                .eventDate(LocalDate.now())
                .build();

        factRepo.save(fact);
    }

 public PageResponse<BranchAggregateDTO> getAllBranchesStats(
         StatsPeriod period,
         Pageable pageable
 ) {
     DateRange range = resolve(period);
     
     Page<BranchAggregateDTO> page = companyRepo.aggregateBranches(
             range.start,
             range.end,
             pageable
     );
     
     return PageResponse.<BranchAggregateDTO>builder()
             .content(page.getContent())
             .page(page.getNumber())
             .size(page.getSize())
             .totalElements(page.getTotalElements())
             .totalPages(page.getTotalPages())
             .build();
 }

    private PageResponse<FinancialFactDTO> toPage(Page<FinancialFact> page) {
        return PageResponse.<FinancialFactDTO>builder()
                .content(page.getContent().stream().map(this::toDto).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    private FinancialFactDTO toDto(FinancialFact f) {
        return FinancialFactDTO.builder()
                .id(f.getId())
                .masterId(f.getMasterId())
                .branchId(f.getBranchId())
                .orderId(f.getOrderId())
                .type(f.getType().name())
                .amount(f.getAmount())
                .eventDate(f.getEventDate())
                .build();
    }

    private DateRange resolve(StatsPeriod period) {
        LocalDate now = LocalDate.now();
        LocalDate start = switch (period) {
            case TODAY -> now;
            case WEEK -> now.minusDays(7);
            case MONTH -> now.minusMonths(1);
            case ALL -> LocalDate.of(1970, 1, 1);
        };
        return new DateRange(start, now);
    }

    private record DateRange(LocalDate start, LocalDate end) {}
}