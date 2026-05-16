package com.system.stats.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BranchStatsDTO {
    private Long branchId;
    private Integer totalOrders;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private Integer totalReturns;
    private BigDecimal netProfit;
}