package com.system.warehouse.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.system.warehouse.entity.StockBalance;

import jakarta.transaction.Transactional;

@Repository
public interface StockBalanceRepository extends JpaRepository<StockBalance, Long> {

    // =========================================================
    // SEARCH BY PART + BRANCH
    // =========================================================

    Optional<StockBalance> findByPartIdAndBranchId(Long partId, Long branchId);

    // =========================================================
    // UPDATE QUANTITY (optimistic safe)
    // =========================================================

    @Modifying
    @Transactional
    @Query("""
           UPDATE StockBalance s
           SET s.quantity = :quantity,
               s.version = s.version + 1
           WHERE s.id = :id
           """)
    int updateQuantityById(Long id, Integer quantity);
    
    
    @Modifying
    @Transactional
    @Query("""
           UPDATE StockBalance s
           SET s.quantity = :quantity,
               s.version = s.version + 1
           WHERE s.id = :id AND s.version = :version
           """)
    int updateQuantityById(Long id, Integer quantity, Long version);

    // =========================================================
    // DELETE ALL BY BRANCH
    // =========================================================

    @Modifying
    @Transactional
    void deleteAllByBranchId(Long branchId);

    // =========================================================
    // DELETE ALL BY PART
    // =========================================================

    @Modifying
    @Transactional
    void deleteAllByPartId(Long partId);
}