package com.system.stats.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "master_daily_stats",
       uniqueConstraints = @UniqueConstraint(columnNames = {"master_id", "stat_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterDailyStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long masterId;

    @Column(nullable = false)
    private Long branchId;

    @Column(nullable = false)
    private LocalDate statDate;

    @Builder.Default
    private Integer orderCount = 0;
    
    @Builder.Default
    private Integer cancelledOrdersCount = 0;
    
    @Builder.Default
    private Integer returnedOrdersCount = 0;

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal totalIncome = BigDecimal.ZERO;
}