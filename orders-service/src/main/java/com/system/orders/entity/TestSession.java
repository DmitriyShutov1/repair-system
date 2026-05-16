package com.system.orders.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "session_at", nullable = false, updatable = false)
    private LocalDateTime sessionAt;

    @PrePersist
    public void prePersist() {
        if (sessionAt == null) {
            sessionAt = LocalDateTime.now();
        }
    }
}
