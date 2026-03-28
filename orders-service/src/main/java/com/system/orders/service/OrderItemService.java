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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderItemService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final OrderService orderService;


    public void setOrderItems(SetOrderItemsRequest request) {

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != Order.Status.CREATED) {
            throw new IllegalStateException("Items can be set only for CREATED orders");
        }

        List<OrderItem> orderItems = request.getItems().stream()
                .map(i -> toEntity(order, i))
                .toList();

        orderItemRepository.saveAll(orderItems);
        
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .oldStatus(order.getStatus()) 
                .newStatus(Order.Status.WAITING_FOR_APPROVAL)
                .changedBy(order.getMasterId())
                .changedAt(Instant.now())
                .build();
        historyRepository.save(history);
        
        

        order.setStatus(Order.Status.WAITING_FOR_APPROVAL);
        order.setUpdatedAt(Instant.now());
        
        

        orderRepository.save(order);
        
        if (order.getWarrantyId() != null) {
        	orderService.confirmOrder(order.getOrderId(), order.getClientId());
        }
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