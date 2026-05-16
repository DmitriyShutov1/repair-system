package com.system.stats.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class FinancialFactDTO {
    private Long id;
    private Long masterId;
    private Long branchId;
    private Long orderId;
    private String type;
    private BigDecimal amount;
    private LocalDate eventDate;
}