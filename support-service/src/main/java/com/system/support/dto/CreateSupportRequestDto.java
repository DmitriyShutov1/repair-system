package com.system.support.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class CreateSupportRequestDto {
    
    private Long masterId;
    
    private Long clientId;

    private Long orderId;

    private String description;
    
    private String deviceSerial;  
    
    private String deviceModel;   

    private List<OrderItemDto> items;
}