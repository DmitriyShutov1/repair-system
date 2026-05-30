
package com.system.stats.service;

import com.system.stats.dto.OperationEventDto;
import com.system.stats.entity.CompanyDailyStats;
import com.system.stats.entity.FinancialFact;
import com.system.stats.entity.MasterDailyStats;
import com.system.stats.repository.CompanyDailyStatsRepository;
import com.system.stats.repository.FinancialFactRepository;
import com.system.stats.repository.MasterDailyStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsProcessingService {

    private final FinancialFactRepository factRepo;
    private final MasterDailyStatsRepository masterRepo;
    private final CompanyDailyStatsRepository companyRepo;

    @Transactional
    public void processEvent(OperationEventDto dto) {

        if (factRepo.existsByEventId(dto.getEventId())) {
            log.warn("Event already processed: {}", dto.getEventId());
            return;
        }

        LocalDate date = dto.getEventTime().toLocalDate();

        MasterDailyStats master = masterRepo
                .findByMasterIdAndStatDate(dto.getMasterId(), date)
                .orElse(MasterDailyStats.builder()
                        .masterId(dto.getMasterId())
                        .branchId(dto.getBranchId())
                        .statDate(date)
                        .build());

        CompanyDailyStats company = companyRepo
                .findByStatDateAndBranchId(date, dto.getBranchId())
                .orElse(CompanyDailyStats.builder()
                        .branchId(dto.getBranchId())
                        .statDate(date)
                        .build());

        BigDecimal factAmount = BigDecimal.ZERO;

        switch (dto.getType()) {

            case ORDER_COMPLETED -> {
                BigDecimal income = nvl(dto.getClientAmount());
                //BigDecimal expenses = nvl(dto.getCostPrice()).add(nvl(dto.getMasterAmount()));
                BigDecimal expenses = nvl(dto.getMasterAmount());

                factAmount = income.subtract(expenses);

                master.setOrderCount(master.getOrderCount() + 1);
                master.setTotalIncome(master.getTotalIncome().add(nvl(dto.getMasterAmount())));

                company.setTotalOrders(company.getTotalOrders() + 1);
                company.setTotalIncome(company.getTotalIncome().add(income));
                company.setTotalExpenses(company.getTotalExpenses().add(expenses));
            }

            case WARRANTY_COMPLETED -> {
                //BigDecimal expenses = nvl(dto.getCostPrice()).add(nvl(dto.getMasterAmount()));
            	BigDecimal expenses = nvl(dto.getMasterAmount());

                factAmount = expenses.negate(); // убыток

                master.setOrderCount(master.getOrderCount() + 1);
                master.setTotalIncome(master.getTotalIncome().add(nvl(dto.getMasterAmount())));

                company.setTotalExpenses(company.getTotalExpenses().add(expenses));
            }

            case REFUND -> {
                BigDecimal refund = nvl(dto.getClientAmount());

                factAmount = refund.negate();

                master.setReturnedOrdersCount(master.getReturnedOrdersCount() + 1);

                company.setTotalReturns(company.getTotalReturns() + 1);
                company.setTotalExpenses(company.getTotalExpenses().add(refund));
            }

            case ORDER_CANCELLED_CLIENT -> {
                BigDecimal income = nvl(dto.getClientAmount());
                //BigDecimal expenses = nvl(dto.getCostPrice()).add(nvl(dto.getMasterAmount()));
                BigDecimal expenses = nvl(dto.getMasterAmount());

                factAmount = income.subtract(expenses);

                master.setOrderCount(master.getOrderCount() + 1);
                master.setTotalIncome(master.getTotalIncome().add(nvl(dto.getMasterAmount())));

                company.setTotalOrders(company.getTotalOrders() + 1);
                company.setTotalIncome(company.getTotalIncome().add(income));
                company.setTotalExpenses(company.getTotalExpenses().add(expenses));
            }

            case ORDER_CANCELLED_MASTER -> {
                BigDecimal cost = nvl(dto.getCostPrice());
                
                master.setOrderCount(master.getOrderCount() + 1);

                if (cost.compareTo(BigDecimal.ZERO) > 0) {
                    master.setCancelledOrdersCount(master.getCancelledOrdersCount() + 1);
                }

                //factAmount = cost.negate();
                factAmount = BigDecimal.ZERO;
                
                company.setTotalOrders(company.getTotalOrders() + 1);
                //company.setTotalExpenses(company.getTotalExpenses().add(cost));
            }
            case PURCHASE -> {

                BigDecimal expenses = nvl(dto.getCostPrice());

                factAmount = expenses.negate();

                company.setTotalExpenses(
                        company.getTotalExpenses().add(expenses)
                );
            }
        }

        FinancialFact fact = FinancialFact.builder()
                .eventId(dto.getEventId())
                .type(dto.getType())
                .masterId(dto.getMasterId())
                .branchId(dto.getBranchId())
                .orderId(dto.getOrderId())
                .amount(factAmount)
                .eventDate(date)
                .build();

        factRepo.save(fact);

        masterRepo.save(master);
        companyRepo.save(company);

        log.info("Event processed: {}, fact amount: {}", dto.getEventId(), factAmount);
    }

    private BigDecimal nvl(BigDecimal val) {
        return val == null ? BigDecimal.ZERO : val;
    }
}