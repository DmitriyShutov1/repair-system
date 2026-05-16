package com.system.orders.repository;

import com.system.orders.entity.Order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderId(Long orderId);

    List<Order> findByClientId(Long clientId);

    List<Order> findByMasterId(Long masterId);

    List<Order> findByStatus(Order.Status status);
    
    List<Order> findByStatusAndMasterId(Order.Status status, Long masterId);
    
    @Query("""
        SELECT COUNT(oi) > 0
        FROM OrderItem oi
        JOIN oi.order o
        WHERE oi.itemId = :itemId
        AND oi.itemType = :itemType
        AND o.status <> 'ISSUED'
    """)
    boolean existsActiveOrderForItem(Long itemId, String itemType);

    
    @Query("""
    	    SELECT COUNT(oi) > 0
    	    FROM OrderItem oi
    	    JOIN oi.order o
    	    WHERE oi.itemId = :itemId
    	    AND oi.itemType = :itemType
    	    AND o.status <> :status
    	""")
    	boolean existsActiveOrderForItem(Long itemId, String itemType, Order.Status status);


    Page<Order> findByStatusAndMasterId(
            Order.Status status,
            Long masterId,
            Pageable pageable
    );

    Page<Order> findByMasterIdAndStatusNot(
            Long masterId,
            Order.Status status,
            Pageable pageable
    );

    Page<Order> findByMasterIdAndStatus(
            Long masterId,
            Order.Status status,
            Pageable pageable
    );

    List<Order> findByMasterIdAndStatusNot(Long masterId, Order.Status status);

    List<Order> findByMasterIdAndStatus(Long masterId, Order.Status status);


    
    Page<Order> findByClientIdAndStatusNot(
            Long clientId,
            Order.Status status,
            Pageable pageable
    );

    Page<Order> findByClientIdAndStatus(
            Long clientId,
            Order.Status status,
            Pageable pageable
    );
    
    List<Order> findByClientIdAndStatusNot(Long clientId, Order.Status status);

    List<Order> findByClientIdAndStatus(Long clientId, Order.Status status);
    
    
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END " +
           "FROM Order o WHERE o.masterId = :masterId AND o.status != :status")
    boolean existsByMasterIdAndStatusNot(Long masterId, Order.Status status);
}
