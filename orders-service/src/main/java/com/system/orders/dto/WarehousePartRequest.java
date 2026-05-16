package com.system.orders.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehousePartRequest {

    private Long id;

    private String name;

    private String articleNumber;

    private String category;

    private Boolean active;

    private BigDecimal costPrice;      
    private BigDecimal clientPrice;    
    private BigDecimal masterPercentage; 

    private Integer quantity;
}