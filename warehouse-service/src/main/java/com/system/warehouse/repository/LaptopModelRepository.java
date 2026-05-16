package com.system.warehouse.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.system.warehouse.entity.*;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
public interface LaptopModelRepository extends JpaRepository<LaptopModel, Long> {

    Optional<LaptopModel> findByIdAndActiveTrue(Long id);

    Page<LaptopModel> findAllByActiveTrue(Pageable pageable);

    Page<LaptopModel> findAllByBrandIgnoreCaseAndActiveTrue(
            String brand,
            Pageable pageable
    );

    Page<LaptopModel> findAllByBrandIgnoreCaseAndReleaseYearAndActiveTrue(
            String brand,
            Integer releaseYear,
            Pageable pageable
    );

    Optional<LaptopModel> findByBrandIgnoreCaseAndModelNameIgnoreCaseAndActiveTrue(
            String brand,
            String modelName
    );

    boolean existsByBrandIgnoreCaseAndModelNameIgnoreCase(String brand, String modelName);

    @Modifying
    @Query("""
           UPDATE LaptopModel l
           SET l.active = false,
               l.version = l.version + 1
           WHERE l.id = :id
           """)
    void softDelete(@Param("id") Long id);
}