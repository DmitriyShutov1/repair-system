package com.system.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "service",
        uniqueConstraints = {
                @UniqueConstraint(name = "service_service_code_key", columnNames = "service_code")
        },
        indexes = {
                @Index(name = "idx_service_category", columnList = "category")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "service_code", nullable = false, length = 100)
    private String serviceCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, columnDefinition = "service_category_enum")
    private ServiceCategory category;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Service service)) return false;
        return id != null && id.equals(service.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}