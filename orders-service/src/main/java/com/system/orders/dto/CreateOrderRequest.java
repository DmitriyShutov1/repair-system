package com.system.orders.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    private Long clientId;

    private Long warrantyId;
    
    private String deviceSerial; 
    
    private String deviceModel;

    private String diagnosticResult;
}