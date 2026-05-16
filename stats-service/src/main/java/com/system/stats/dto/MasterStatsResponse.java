package com.system.stats.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MasterStatsResponse {

    private Long masterId;

    private String period; 

    private Integer totalOrders;
    private Integer cancelledOrders;
    private Integer returnedOrders;

    private BigDecimal totalIncome;
}

