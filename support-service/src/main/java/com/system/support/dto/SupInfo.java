package com.system.support.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.system.support.entity.SupportRequestStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupInfo {
	private Long id;
    
    private Long masterId;

    private Long branchId;
    
    private Long clientId;
    
    private Long orderId;

    private String description;
    
    private BigDecimal refundCost;
    
    private Instant createdAt;
    
    private Instant completedAt;
    
    private SupportRequestStatus status;

    private List<ItemInfo> items;
}