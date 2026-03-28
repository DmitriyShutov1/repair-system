package com.system.warehouse.dto;

import java.math.BigDecimal;


import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class ServiceWithPriceDto {

    private Long id;
    private String name;
    private String serviceCode;
    private String category;
    private Boolean active;

    private BigDecimal costPrice;
}