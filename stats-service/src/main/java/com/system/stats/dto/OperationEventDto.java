package com.system.stats.dto;

import lombok.AllArgsConstructor;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.system.stats.entity.FinancialFact.OperationType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationEventDto {
    private UUID eventId;           
    private OperationType type;     
    private LocalDateTime eventTime; 
    
    private Long branchId;           
    private Long masterId;           
    private Long originalMasterId;
    private Long supportId;
    private Long orderId;
    
    private BigDecimal clientAmount;  
    private BigDecimal costPrice;     
    private BigDecimal masterAmount;  
}