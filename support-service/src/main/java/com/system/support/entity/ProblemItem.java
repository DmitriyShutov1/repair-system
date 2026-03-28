package com.system.support.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "problem_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProblemItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "support_request_id", nullable = false)
    @JsonIgnore
    private SupportRequest supportRequest;

    @Column(name = "item_type", nullable = false)
    private String itemType;

    @Column(nullable = false)
    private String name;

    private String category;

    @Column(name = "sell_price", precision = 12, scale = 2)
    private BigDecimal sellPrice;

    @Column(nullable = false)
    private Integer quantity;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}