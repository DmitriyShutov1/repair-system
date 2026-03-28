package com.system.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PartStockWaitingDto {

    private Long partId;
    private String name;
    private String articleNumber;
    private String category;
    private Boolean active;

    private Integer stockQuantity;
    private Long waitingQuantity;
}