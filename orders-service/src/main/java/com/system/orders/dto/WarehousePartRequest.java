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

 // Ценовые поля - все три
    private BigDecimal costPrice;      // себестоимость
    private BigDecimal clientPrice;    // цена для клиента
    private BigDecimal masterPercentage; // процент мастера

    private Integer quantity;
}