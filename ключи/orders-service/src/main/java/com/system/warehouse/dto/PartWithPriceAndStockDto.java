package com.system.warehouse.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PartWithPriceAndStockDto {

    private Long id;
    private String name;
    private String articleNumber;
    private String category;
    private Boolean active;

    private BigDecimal costPrice;
    private Integer quantity;
}