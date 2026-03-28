package com.system.support.dto;

import lombok.*;

import java.math.BigDecimal;

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