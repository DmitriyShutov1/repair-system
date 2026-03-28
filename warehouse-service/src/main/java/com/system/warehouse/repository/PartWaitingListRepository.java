package com.system.warehouse.repository;

import com.system.warehouse.entity.PartWaitingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface PartWaitingListRepository extends JpaRepository<PartWaitingList, Long> {

    /**
     * Поиск записи по part_id и branch_id
     */
    @Query("""
            SELECT w
            FROM PartWaitingList w
            WHERE w.part.id = :partId
              AND w.branchId = :branchId
              AND w.closed = false
            ORDER BY w.createdAt 
           """)
    List<PartWaitingList> findActiveByPartIdAndBranchId(Long partId, Long branchId);

    
    Optional<PartWaitingList> findByOrderIdAndPartIdAndBranchIdAndClosedFalse(
            Long orderId,
            Long partId,
            Long branchId
    );

    /**
     * Изменение quantity
     */
    @Modifying
    @Transactional
    @Query("""
            UPDATE PartWaitingList w
            SET w.requiredQuantity = :quantity
            WHERE w.id = :id
           """)
    int updateQuantity(Long id, Integer quantity);
    
    
    @Modifying
    @Transactional
    @Query("""
            UPDATE PartWaitingList w
            SET w.closed = true
            WHERE w.id = :id
           """)
    int setThisClosed(Long id);


    /**
     * Агрегация количества по order_id
     */
    @Query("""
            SELECT COALESCE(SUM(w.requiredQuantity),0)
            FROM PartWaitingList w
            WHERE w.orderId = :orderId
              AND w.closed = false
           """)
    Integer sumRequiredQuantityByOrderId(Long orderId);


    /**
     * Найти запись по id (явно Optional)
     */
    Optional<PartWaitingList> findById(Long id);

    
    @Query("""
            SELECT w
            FROM PartWaitingList w
            WHERE w.part.id = :partId
              AND w.closed = false
           """)
    List<PartWaitingList> findActiveByPartId(Long partId);
}