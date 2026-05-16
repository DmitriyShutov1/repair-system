package com.system.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "stock_movement",
        indexes = {
                @Index(name = "idx_movement_part", columnList = "part_id"),
                @Index(name = "idx_movement_branch", columnList = "branch_id"),
                @Index(name = "idx_movement_order", columnList = "order_id"),
                @Index(name = "idx_movement_created", columnList = "created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Связь оставляем LAZY — движения читаются часто,
     * но сам Part нужен редко.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "part_id",
            foreignKey = @ForeignKey(name = "fk_movement_part"))
    private Part part;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;
    
    @Column(name = "master_id", nullable = false)
    private Long masterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, columnDefinition = "movement_type_enum")
    private MovementType movementType;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}