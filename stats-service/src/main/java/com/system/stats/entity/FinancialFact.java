package com.system.stats.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "financial_fact")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialFact {
	
	public enum OperationType {
	    ORDER_COMPLETED,
	    ORDER_CANCELLED_MASTER,
	    ORDER_CANCELLED_CLIENT,
	    WARRANTY_COMPLETED,
	    REFUND,
	    BONUS,
	    PENALTY,
	    PURCHASE
	}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long masterId;

    @Column(nullable = false)
    private Long branchId;

    private Long orderId;
    
    private UUID eventId;       

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationType type;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate eventDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}