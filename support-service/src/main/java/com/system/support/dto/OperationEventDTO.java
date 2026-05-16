package com.system.support.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationEventDTO {

    private UUID eventId;
    private OperationType type;
    private LocalDateTime eventTime;

    private Long branchId;
    private Long masterId;
    private Long originalMasterId;
    private Long supportId;
    private Long orderId;

    private BigDecimal clientAmount;
    private BigDecimal costPrice;
    private BigDecimal masterAmount;

    public enum OperationType {
        ORDER_COMPLETED,
        ORDER_CANCELLED_MASTER,
        ORDER_CANCELLED_CLIENT,
        WARRANTY_COMPLETED,
        REFUND
    }
}