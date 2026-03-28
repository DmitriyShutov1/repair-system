package com.system.orders.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.system.orders.entity.OrderItem;
import com.system.orders.entity.OrderStatusHistory;
import com.system.orders.entity.Order.Status;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long orderId;

    private Long clientId;

    private Long masterId;

    private Long warrantyId;

    private Status status;

    private String diagnosticResult;

    private Boolean clientApproved;

    private String pickupCode;

    private Instant createdAt;

    private Instant updatedAt;

    private Instant completedAt;
}