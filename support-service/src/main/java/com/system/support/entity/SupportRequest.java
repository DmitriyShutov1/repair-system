package com.system.support.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "support_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "support_id", nullable = false)
    private Long supportId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "client_id", nullable = false)
    private Long clientId;
    
    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "master_id")
    private Long masterId;

    @Column(name = "completed_by_master_id")
    private Long completedByMasterId;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportRequestStatus status;

    @Column(precision = 12, scale = 2)
    private BigDecimal cost;

    @Column(name = "master_cost", precision = 12, scale = 2)
    private BigDecimal masterCost;

    @Column(name = "refund_cost", precision = 12, scale = 2)
    private BigDecimal refundCost;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @OneToMany(mappedBy = "supportRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProblemItem> items = new ArrayList<>();
}