
package com.system.stats.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class BranchAggregateDTO {
    private Long branchId;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private Integer totalOrders;
    private Integer totalReturns;
    
    public BranchAggregateDTO(
            Long branchId,
            BigDecimal totalIncome,
            BigDecimal totalExpenses,
            Long totalOrders,
            Long totalReturns
    ) {
        this.branchId = branchId;
        this.totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        this.totalExpenses = totalExpenses != null ? totalExpenses : BigDecimal.ZERO;
        this.totalOrders = totalOrders != null ? totalOrders.intValue() : 0;
        this.totalReturns = totalReturns != null ? totalReturns.intValue() : 0;
    }

    public BigDecimal getNetProfit() {
        return totalIncome.subtract(totalExpenses);
    }
}