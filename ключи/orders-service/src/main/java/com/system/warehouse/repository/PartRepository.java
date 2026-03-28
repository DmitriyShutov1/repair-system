package com.system.warehouse.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.system.warehouse.entity.*;

@Repository
public interface PartRepository extends JpaRepository<Part, Long>, PartRepositoryCustom {
    
    Optional<Part> findByArticleNumber(String articleNumber);

    Page<Part> findAllByCategoryAndActive(
            PartCategory category,
            Boolean active,
            Pageable pageable
    );

    boolean existsByArticleNumber(String articleNumber);
}