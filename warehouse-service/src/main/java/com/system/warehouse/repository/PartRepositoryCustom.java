package com.system.warehouse.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.system.warehouse.dto.PartStockWaitingDto;
import com.system.warehouse.dto.PartWithPriceAndStockDto;
import com.system.warehouse.entity.*;

public interface PartRepositoryCustom {

    Page<Part> searchByName(String query, Pageable pageable);

    void softDelete(Long id);

    Part update(Part part);
    
    Page<PartWithPriceAndStockDto> searchWithPriceAndStockByName(
            String query,
            Boolean active,
            Long branchId,
            Pageable pageable
    );

    Page<PartWithPriceAndStockDto> findWithPriceAndStockByCategory(
            PartCategory category,
            Boolean active,
            Long branchId,
            Pageable pageable
    );

    Optional<PartWithPriceAndStockDto> findWithPriceAndStockByArticle(
            String articleNumber,
            Boolean active,
            Long branchId
    );
    
    Page<Part> findPartsWithoutActivePrice(Pageable pageable);

    Page<PartStockWaitingDto> findPartsWithStockAndWaiting(
            Long branchId,
            Pageable pageable
    );
    
    Optional<PartStockWaitingDto> findPartWithStockAndWaitingByArticle(
            String articleNumber,
            Long branchId
    );
}