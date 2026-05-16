package com.system.orders.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetOrderItemsRequest {

    private Long orderId;

    private List<OrderItemRequest> items;
}