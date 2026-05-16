package com.system.warehouse.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PartWithPriceAndStockDto {

    private Long id;
    private String name;
    private String articleNumber;
    private String category;
    private Boolean active;
    
    private BigDecimal clientPrice;      // добавляем
    private BigDecimal masterPercentage; // добавляем
    private BigDecimal costPrice;
    private Integer quantity;
}