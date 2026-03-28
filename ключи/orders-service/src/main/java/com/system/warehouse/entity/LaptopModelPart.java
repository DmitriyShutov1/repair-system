package com.system.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "laptop_model_part",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_laptop_part",
                        columnNames = {"laptop_model_id", "part_id"})
        },
        indexes = {
                @Index(name = "idx_cmp_part", columnList = "part_id"),
                @Index(name = "idx_cmp_laptop", columnList = "laptop_model_id")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class LaptopModelPart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "laptop_model_id",
            foreignKey = @ForeignKey(name = "fk_cmp_laptop"))
    private LaptopModel laptopModel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "part_id",
            foreignKey = @ForeignKey(name = "fk_cmp_part"))
    private Part part;
}