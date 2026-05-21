package com.system.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "part",
        uniqueConstraints = {
                @UniqueConstraint(name = "part_article_number_key", columnNames = "article_number")
        },
        indexes = {
                @Index(name = "idx_part_category", columnList = "category")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Part {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "article_number", nullable = false, length = 100)
    private String articleNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false/*, columnDefinition = "part_category_enum"*/)
    private PartCategory category;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Part part)) return false;
        return id != null && id.equals(part.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}