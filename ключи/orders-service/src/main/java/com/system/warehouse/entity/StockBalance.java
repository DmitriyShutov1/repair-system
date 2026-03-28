package com.system.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "stock_balance",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_stock_part_branch",
                        columnNames = {"part_id", "branch_id"})
        },
        indexes = {
                @Index(name = "idx_stock_branch", columnList = "branch_id"),
                @Index(name = "idx_stock_part", columnList = "part_id")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class StockBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "part_id",
            foreignKey = @ForeignKey(name = "fk_stock_part"))
    private Part part;

    /**
     * branch — это ID внешнего сервиса.
     * Нам не нужна связь @ManyToOne!
     */
    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}