package com.system.orders.repository;

import com.system.orders.entity.OrderStatusHistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    List<OrderStatusHistory> findByOrderOrderIdOrderByChangedAtAsc(Long orderId);

    Optional<OrderStatusHistory> findTopByOrderOrderIdOrderByChangedAtDesc(Long orderId);

    List<OrderStatusHistory> findByChangedBy(Long changedBy);

}