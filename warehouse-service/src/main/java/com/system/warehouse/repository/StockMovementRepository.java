package com.system.warehouse.repository;

import com.system.warehouse.entity.MovementType;
import com.system.warehouse.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    // Все движения по детали
    List<StockMovement> findByPartIdOrderByCreatedAtDesc(Long partId);

    // Все движения по филиалу
    List<StockMovement> findByBranchIdOrderByCreatedAtDesc(Long branchId);

    // Все движения по заказу
    List<StockMovement> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    // Все движения по типу
    List<StockMovement> findByMovementTypeOrderByCreatedAtDesc(MovementType type);

    // Движения за период (с пагинацией)
    Page<StockMovement> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    // Движения по мастеру
    List<StockMovement> findByMasterIdOrderByCreatedAtDesc(Long masterId);

    // Движения по детали и типу
    List<StockMovement> findByPartIdAndMovementTypeOrderByCreatedAtDesc(Long partId, MovementType type);

    // Сумма прихода по детали за период
    @Query("SELECT COALESCE(SUM(sm.quantity), 0) FROM StockMovement sm " +
           "WHERE sm.part.id = :partId " +
           "AND sm.movementType = :type " +
           "AND sm.createdAt BETWEEN :from AND :to")
    Integer sumQuantityByPartAndTypeAndPeriod(
            @Param("partId") Long partId,
            @Param("type") MovementType type,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
    
    List<StockMovement> findByOrderIdAndMovementType(Long orderId, MovementType type);

    // Последние N движений по филиалу
    Page<StockMovement> findByBranchIdOrderByCreatedAtDesc(Long branchId, Pageable pageable);
    
    @Query("SELECT COALESCE(SUM(sm.quantity), 0) FROM StockMovement sm " +
    	       "WHERE sm.orderId = :orderId AND sm.part.id = :partId AND sm.movementType = :type")
    	Integer sumQuantityByOrderIdAndPartIdAndMovementType(
    	        @Param("orderId") Long orderId,
    	        @Param("partId") Long partId,
    	        @Param("type") MovementType type);
}