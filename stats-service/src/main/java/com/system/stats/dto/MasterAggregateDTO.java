
package com.system.stats.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class MasterAggregateDTO {
    private Integer totalOrders;
    private Integer cancelledOrders;
    private Integer returnedOrders;
    private BigDecimal totalIncome;
    
    public MasterAggregateDTO(
            Long totalOrders,
            Long cancelledOrders,
            Long returnedOrders,
            BigDecimal totalIncome
    ) {
        this.totalOrders = totalOrders != null ? totalOrders.intValue() : 0;
        this.cancelledOrders = cancelledOrders != null ? cancelledOrders.intValue() : 0;
        this.returnedOrders = returnedOrders != null ? returnedOrders.intValue() : 0;
        this.totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
    }
}