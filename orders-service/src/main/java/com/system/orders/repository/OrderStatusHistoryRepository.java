package com.system.orders.repository;

import com.system.orders.entity.OrderStatusHistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    // Получить всю историю изменения статусов заказа
    List<OrderStatusHistory> findByOrderOrderIdOrderByChangedAtAsc(Long orderId);

    // Получить последнюю запись статуса
    Optional<OrderStatusHistory> findTopByOrderOrderIdOrderByChangedAtDesc(Long orderId);

    // Найти изменения, сделанные конкретным пользователем
    List<OrderStatusHistory> findByChangedBy(Long changedBy);

}