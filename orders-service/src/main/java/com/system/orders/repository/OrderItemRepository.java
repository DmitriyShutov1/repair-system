package com.system.orders.repository;

import com.system.orders.entity.OrderItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderOrderId(Long orderId);

    List<OrderItem> findByItemId(Long itemId);

    List<OrderItem> findByItemType(String itemType);

    void deleteByOrderOrderId(Long orderId);
}