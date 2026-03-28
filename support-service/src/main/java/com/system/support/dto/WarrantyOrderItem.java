package com.system.support.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantyOrderItem {

    private Long id;

    private String itemType;

    private String name;

    private String serviceCode;

    private String category;

    private BigDecimal costPrice;
    
    private BigDecimal sellPrice;
    
    private BigDecimal masterPercentage;

    private Integer quantity;
}