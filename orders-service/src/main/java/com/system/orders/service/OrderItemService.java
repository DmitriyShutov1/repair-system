package com.system.orders.service;

import com.system.orders.client.WarehouseClient;
import com.system.orders.dto.OrderItemRequest;
import com.system.orders.dto.SetOrderItemsRequest;
import com.system.orders.dto.WarehousePartRequest;
import com.system.orders.entity.Order;
import com.system.orders.entity.OrderItem;
import com.system.orders.entity.OrderStatusHistory;
import com.system.orders.repository.OrderItemRepository;
import com.system.orders.repository.OrderRepository;
import com.system.orders.repository.OrderStatusHistoryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderItemService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final OrderService orderService;
    private final WarehouseClient warehouseClient;

    public void setOrderItems(SetOrderItemsRequest request) {

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == Order.Status.ISSUED 
                || order.getStatus() == Order.Status.COMPLETED 
                || order.getStatus() == Order.Status.CANCELLED_BY_MASTER 
                || order.getStatus() == Order.Status.CANCELLED_BY_CLIENT) {
            throw new IllegalStateException("Zakaz uze na stadii zavershenia");
        }

        List<OrderItem> existingItems = orderItemRepository.findByOrderOrderId(order.getOrderId());

        for (OrderItemRequest newItem : request.getItems()) {

            Optional<OrderItem> existingOpt = existingItems.stream()
                    .filter(e -> e.getItemType().equals(newItem.getItemType()) 
                            && e.getItemId().equals(newItem.getId()))
                    .findFirst();

            if (existingOpt.isPresent()) {
                OrderItem existing = existingOpt.get();
                existing.setQuantity(existing.getQuantity() + newItem.getQuantity());
                existing.setSellPrice(newItem.getSellPrice());
                existing.setCostPrice(newItem.getCostPrice());
                existing.setMasterPercentage(newItem.getMasterPercentage());
            } else {
                
                OrderItem newEntity = toEntity(order, newItem);
                existingItems.add(newEntity);
            }
        }

        orderItemRepository.saveAll(existingItems);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .oldStatus(order.getStatus())
                .newStatus(Order.Status.WAITING_FOR_APPROVAL)
                .changedBy(order.getMasterId())
                .changedAt(Instant.now())
                .build();
        historyRepository.save(history);

        order.setStatus(Order.Status.WAITING_FOR_APPROVAL);
        order.setClientApproved(false);
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);

        if (order.getWarrantyId() != null) {
            orderService.confirmOrder(order.getOrderId(), order.getClientId());
        }
    }
    
    public void removeOrderItems(Long orderId, List<Long> itemIdsToRemove, Long masterId, Long branchId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != Order.Status.WAITING_FOR_PARTS 
                && order.getStatus() != Order.Status.IN_PROGRESS
                && order.getStatus() != Order.Status.WAITING_FOR_APPROVAL) {
            throw new IllegalStateException(
                "Items can be removed only from WAITING_FOR_APPROVAL, WAITING_FOR_PARTS or IN_PROGRESS orders");
        }

        List<OrderItem> itemsToRemove = orderItemRepository.findAllById(itemIdsToRemove);

        for (OrderItem item : itemsToRemove) {
            if (!item.getOrder().getOrderId().equals(orderId)) {
                throw new IllegalStateException("Item " + item.getId() + " does not belong to order " + orderId);
            }
        }

        List<WarehousePartRequest> partsToReturn = itemsToRemove.stream()
                .filter(i -> "PART".equals(i.getItemType()))
                .map(this::toWarehousePart) 
                .toList();

        if ((order.getStatus() == Order.Status.WAITING_FOR_PARTS 
        	     || order.getStatus() == Order.Status.IN_PROGRESS)
        	    && !partsToReturn.isEmpty()) {

        	warehouseClient.cancelReserve(orderId, branchId, masterId, partsToReturn);
        	}

        orderItemRepository.deleteAll(itemsToRemove);
        
        List<OrderItem> remaining = orderItemRepository.findByOrderOrderId(orderId);

        if (remaining.isEmpty()) {
            OrderStatusHistory history = OrderStatusHistory.builder()
                    .order(order)
                    .oldStatus(order.getStatus())
                    .newStatus(Order.Status.CREATED)
                    .changedBy(masterId)
                    .changedAt(Instant.now())
                    .build();
            historyRepository.save(history);

            order.setStatus(Order.Status.CREATED);
            order.setClientApproved(false);
            order.setUpdatedAt(Instant.now());
            orderRepository.save(order);

        } else if (!partsToReturn.isEmpty()) {
            order.setStatus(Order.Status.WAITING_FOR_APPROVAL);
            order.setClientApproved(false);
            order.setUpdatedAt(Instant.now());

            OrderStatusHistory history = OrderStatusHistory.builder()
                    .order(order)
                    .oldStatus(order.getStatus())
                    .newStatus(Order.Status.WAITING_FOR_APPROVAL)
                    .changedBy(masterId)
                    .changedAt(Instant.now())
                    .build();
            historyRepository.save(history);

            orderRepository.save(order);

            orderService.confirmOrder(orderId, order.getClientId());
        }

    }
    
    private WarehousePartRequest toWarehousePart(OrderItem item) {

        return WarehousePartRequest.builder()
                .id(item.getItemId())
                .name(item.getName())
                .articleNumber(item.getArticleNumber())
                .category(item.getCategory())
                .active(true)
                .costPrice(null)
                .quantity(item.getQuantity())
                .build();
    }


    private OrderItem toEntity(Order order, OrderItemRequest item) {

        return OrderItem.builder()
                .order(order)
                .itemType(item.getItemType())
                .itemId(item.getId())
                .name(item.getName())
                .articleNumber(item.getServiceCode())
                .category(item.getCategory())
                .costPrice(item.getCostPrice())        
                .sellPrice(item.getSellPrice())
                .masterPercentage(item.getMasterPercentage()) 
                .quantity(item.getQuantity())
                .createdAt(Instant.now())
                .build();
    }
}