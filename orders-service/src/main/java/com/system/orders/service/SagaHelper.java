package com.system.orders.service;

import com.system.orders.client.*;


import com.system.orders.dto.CreateOrderRequest;
import com.system.orders.dto.OrderDetailsDto;
import com.system.orders.dto.OrderItemDto;
import com.system.orders.dto.WarehousePartRequest;
import com.system.orders.entity.Order;
import com.system.orders.entity.OrderItem;
import com.system.orders.entity.OrderStatusHistory;
import com.system.orders.repository.OrderItemRepository;
import com.system.orders.repository.OrderRepository;
import com.system.orders.repository.OrderStatusHistoryRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SagaHelper {
	 private final OrderRepository orderRepository;
	 private final OrderItemRepository orderItemRepository;
	 private final OrderStatusHistoryRepository historyRepository;
	 
	 public Order finalizeOrder(Order order, Boolean reserved, Long clientId) {

	        order.setClientApproved(true);

	        if (reserved) {
	            order.setStatus(Order.Status.WAITING_FOR_PARTS);
	        } else {
	            order.setStatus(Order.Status.IN_PROGRESS);
	        }

	        OrderStatusHistory history = OrderStatusHistory.builder()
	                .order(order)
	                .oldStatus(Order.Status.WAITING_FOR_APPROVAL)
	                .newStatus(order.getStatus())
	                .changedBy(clientId)
	                .changedAt(Instant.now())
	                .build();

	        historyRepository.save(history);

	        order.setUpdatedAt(Instant.now());

	        return orderRepository.save(order);
	    }
	    
	 
	    public Order finalizeCancel(
	            Order order,
	            List<OrderItem> removedItems,
	            boolean cancelledByClient) {

	        if (!removedItems.isEmpty()) {
	            orderItemRepository.deleteAll(removedItems);
	        }

	        OrderStatusHistory history = OrderStatusHistory.builder()
	                .order(order)
	                .oldStatus(order.getStatus())
	                .changedBy(order.getMasterId())
	                .changedAt(Instant.now())
	                .build();

	        order.setStatus(
	                cancelledByClient
	                        ? Order.Status.CANCELLED_BY_CLIENT
	                        : Order.Status.CANCELLED_BY_MASTER
	        );

	        order.setUpdatedAt(Instant.now());

	        history.setNewStatus(order.getStatus());

	        historyRepository.save(history);

	        return orderRepository.save(order);
	    }
	
}





