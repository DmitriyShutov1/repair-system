package com.system.stats.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "company_daily_stats",
       uniqueConstraints = @UniqueConstraint(columnNames = {"stat_date", "branch_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDailyStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate statDate;
    
    @Column(nullable = false)
    private Long branchId;

    @Builder.Default
    private Integer totalOrders = 0;

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal totalIncome = BigDecimal.ZERO;

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal totalExpenses = BigDecimal.ZERO;
    
    @Builder.Default
    private Integer totalReturns = 0;
}