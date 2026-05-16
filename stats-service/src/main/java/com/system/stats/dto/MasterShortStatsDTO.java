
package com.system.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterShortStatsDTO {
    private Long masterId;
    private Long branchId;
    private Integer totalOrders;
    private Integer cancelledOrders;
    private Integer returnedOrders;
    private BigDecimal totalIncome;
    
    public MasterShortStatsDTO(
            Long masterId,
            Long branchId,
            Long totalOrders,
            Long cancelledOrders,
            Long returnedOrders,
            BigDecimal totalIncome
    ) {
        this.masterId = masterId;
        this.branchId = branchId;
        this.totalOrders = totalOrders != null ? totalOrders.intValue() : 0;
        this.cancelledOrders = cancelledOrders != null ? cancelledOrders.intValue() : 0;
        this.returnedOrders = returnedOrders != null ? returnedOrders.intValue() : 0;
        this.totalIncome = totalIncome;
    }
}