package com.system.support.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSupportRequestDto {
    
    private Long masterId;
    
    private Long clientId;

    private Long orderId;

    private String description;

    private List<OrderItemDto> items;
}