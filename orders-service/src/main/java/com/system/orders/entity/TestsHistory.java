//package com.system.orders.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//
//@Entity
//@Table(name = "tests_history")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class TestsHistory {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "order_id", nullable = false)
//    private Long orderId;
//
//    @Column(name = "test_id", nullable = false)
//    private Long testId;
//
//    @Column(nullable = false)
//    private Boolean passed;
//
//    @Column(name = "tested_at", nullable = false, updatable = false)
//    private LocalDateTime testedAt;
//    
//    @PrePersist
//    public void prePersist() {
//        if (testedAt == null) {
//            testedAt = LocalDateTime.now();
//        }
//    }
//}