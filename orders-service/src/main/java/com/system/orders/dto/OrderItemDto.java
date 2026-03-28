package com.system.orders.dto;

import java.math.BigDecimal;
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
public class OrderItemDto {

    private Long id;
    private String itemType;
    private Long itemId;

    private String name;
    private String articleNumber;
    private String category;

    private BigDecimal sellPrice;
    private Integer quantity;
}