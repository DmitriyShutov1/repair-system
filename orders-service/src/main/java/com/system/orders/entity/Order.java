package com.system.orders.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    public enum Status {
        CREATED,
        WAITING_FOR_PARTS,
        WAITING_FOR_APPROVAL,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED_BY_CLIENT,
        CANCELLED_BY_MASTER,
        ISSUED,
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "master_id")
    private Long masterId;
    
    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "warranty_id")
    private Long warrantyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "diagnostic_result")
    private String diagnosticResult;

    @Column(name = "client_approved")
    private Boolean clientApproved;

    @Column(name = "pickup_code")
    private String pickupCode;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();
}