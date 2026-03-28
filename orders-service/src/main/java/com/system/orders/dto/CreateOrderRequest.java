package com.system.orders.dto;

import lombok.*;

//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class CreateOrderRequest {
//
//    private Long clientId;
//
//    private Long masterId;
//
//    private Long warrantyId;
//
//    private String diagnosticResult;
//}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    private Long clientId;

    private Long warrantyId;

    private String diagnosticResult;
}