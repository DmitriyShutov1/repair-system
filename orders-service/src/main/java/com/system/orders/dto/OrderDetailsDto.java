package com.system.orders.dto;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@AllArgsConstructor
@Builder
public class OrderDetailsDto {

    private Long orderId;
    private Long clientId;
    private Long masterId;
    
    private Long warrantyId;
    private String status;
    private String diagnosticResult;
    
    private Instant createdAt;
    private Instant completedAt;
    
    private List<OrderItemDto> items;
}