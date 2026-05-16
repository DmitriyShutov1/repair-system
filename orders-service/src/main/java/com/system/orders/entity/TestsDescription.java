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
//@Table(name = "tests_description")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class TestsDescription {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false)
//    private String name;
//
//    @Column(columnDefinition = "TEXT")
//    private String description;
//
//    @Column(name = "is_required", nullable = false)
//    private Boolean isRequired = true;
//
//    @Column(name = "created_at", nullable = false, updatable = false)
//    private LocalDateTime createdAt;
//    
//    @PrePersist
//    public void prePersist() {
//        if (createdAt == null) {
//        	createdAt = LocalDateTime.now();
//        }
//    }
//}