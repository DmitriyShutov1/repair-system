package com.system.orders.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "item_type", nullable = false)
    private String itemType;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private String name;

    @Column(name = "article_number")
    private String articleNumber;

    private String category;

    @Column(name = "sell_price", nullable = false)
    private BigDecimal sellPrice;
    
    @Column(name = "cost_price", nullable = false)
    private BigDecimal costPrice;

    @Column(name = "master_percentage", nullable = false)
    private BigDecimal masterPercentage;
    
    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}