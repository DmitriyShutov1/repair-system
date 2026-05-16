package com.system.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "part_waiting_list",
        indexes = {
                @Index(name = "idx_wait_order", columnList = "order_id"),
                @Index(name = "idx_wait_part", columnList = "part_id"),
                @Index(name = "idx_wait_branch", columnList = "branch_id")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PartWaitingList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Заказ приходит из другого сервиса — просто ID.
     */
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "part_id",
            foreignKey = @ForeignKey(name = "fk_wait_part"))
    private Part part;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "required_quantity", nullable = false)
    private Integer requiredQuantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_closed", nullable = false)
    private boolean closed;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.closed = false;
    }
    
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public void close() {
        this.closed = true;
    }
}