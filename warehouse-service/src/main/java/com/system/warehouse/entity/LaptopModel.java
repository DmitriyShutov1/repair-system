package com.system.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "laptop_model",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_laptop", columnNames = {"brand", "model_name"})
        },
        indexes = {
                @Index(name = "idx_laptop_brand", columnList = "brand")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class LaptopModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "brand", nullable = false, length = 100)
    private String brand;

    @Column(name = "model_name", nullable = false, length = 150)
    private String modelName;

    @Column(name = "model_series_code", length = 100)
    private String modelSeriesCode;

    @Column(name = "release_year")
    private Integer releaseYear;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    // equals/hashCode ТОЛЬКО по id — важно для Hibernate
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LaptopModel that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}