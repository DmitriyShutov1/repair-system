package com.system.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "pricing_policy",
        indexes = {
                @Index(name = "idx_price_part", columnList = "part_id"),
                @Index(name = "idx_price_service", columnList = "service_id"),
                @Index(name = "idx_price_effective", columnList = "effective_from, effective_to")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PricingPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Либо part, либо service (см. CHECK constraint в БД)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id",
            foreignKey = @ForeignKey(name = "fk_price_part"))
    private Part part;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id",
            foreignKey = @ForeignKey(name = "fk_price_service"))
    private Service service;

    @Column(name = "cost_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "client_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal clientPrice;

    @Column(name = "master_percentage", precision = 5, scale = 2)
    private BigDecimal masterPercentage;

    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public boolean isActiveAt(LocalDateTime moment) {
        return !effectiveFrom.isAfter(moment) &&
                (effectiveTo == null || effectiveTo.isAfter(moment));
    }
}