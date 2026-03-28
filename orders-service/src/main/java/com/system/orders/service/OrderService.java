package com.system.orders.service;

import com.system.orders.client.*;


import com.system.orders.dto.CreateOrderRequest;
import com.system.orders.dto.OrderDetailsDto;
import com.system.orders.dto.OrderItemDto;
import com.system.orders.dto.OrderItemRequest;
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
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository historyRepository;
    
    //добавил
    private final SupportClient supportClient;
    
    
    private final WarehouseClient warehouseClient;
    private final UsersClient usersClient;
    private final SagaHelper saga;
    
    
    //сделать SAGA компенсаторную
    @Transactional
    public Order createOrder(CreateOrderRequest request, Long userId,
            Long branchId,
            String role) {

    	if(!usersClient.userExists(request.getClientId())) {
    		throw new IllegalStateException("Client not found");
    	}
    	
        Order order = Order.builder()
                .clientId(request.getClientId())
                .masterId(userId)
                .warrantyId(request.getWarrantyId())
                .branchId(branchId)
                .diagnosticResult(request.getDiagnosticResult())
                .status(Order.Status.CREATED)
                .clientApproved(false)
                .pickupCode(generatePickupCode())
                .createdAt(Instant.now())
                .build();
        orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .oldStatus(null) 
                .newStatus(Order.Status.CREATED)
                .changedBy(userId)
                .changedAt(Instant.now())
                .build();
        historyRepository.save(history);
        
        if (order.getWarrantyId() != null) {

            Boolean started = supportClient.startWarranty(
                    order.getWarrantyId(),
                    userId
            );

            if (started == null) {
                throw new RuntimeException("Support service unavailable");
            }
        }
        
        return order;
    }

    private String generatePickupCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
//
//    public Order confirmOrder(Long orderId,Long clientId) {
//
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new RuntimeException("Order not found"));
//
//        if (order.getStatus() != Order.Status.WAITING_FOR_APPROVAL) {
//            throw new IllegalStateException("Order not waiting for approval");
//        }
//        Long branchId = order.getBranchId();
//
//        List<OrderItem> items = orderItemRepository.findByOrderOrderId(orderId);
//
//        List<OrderItem> parts = items.stream()
//                .filter(i -> "PART".equals(i.getItemType()))
//                .toList();
//
//        List<WarehousePartRequest> warehouseParts = parts.stream()
//                .map(this::toWarehousePart)
//                .toList();
//
//        Boolean reserved = warehouseClient.reserveParts(orderId, branchId, order.getMasterId(),  warehouseParts);
//
//        if (reserved == null) {
//            throw new RuntimeException("Warehouse error");
//        }
////        if (reserved == null) {
////
////        	throw new RuntimeException("Warehouse troubles");
////        }
//        
//        order.setClientApproved(true);
//        
//        if (reserved) {
//            order.setStatus(Order.Status.WAITING_FOR_PARTS);
//        } else {
//            order.setStatus(Order.Status.IN_PROGRESS);
//        }
//        OrderStatusHistory history = OrderStatusHistory.builder()
//                .order(order)
//                .oldStatus(Order.Status.WAITING_FOR_APPROVAL) 
//                .newStatus(order.getStatus())
//                .changedBy(clientId)
//                .changedAt(Instant.now())
//                .build();
//        historyRepository.save(history);
//        order.setUpdatedAt(Instant.now());
//
//        return orderRepository.save(order);
//    }
    
    public Order confirmOrder(Long orderId, Long clientId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != Order.Status.WAITING_FOR_APPROVAL) {
            throw new IllegalStateException("Order not waiting for approval");
        }

        Long branchId = order.getBranchId();

        List<OrderItem> items = orderItemRepository.findByOrderOrderId(orderId);

        List<WarehousePartRequest> warehouseParts = items.stream()
                .filter(i -> "PART".equals(i.getItemType()))
                .map(this::toWarehousePart)
                .toList();

        Boolean reserved = warehouseClient.reserveParts(
                orderId,
                branchId,
                order.getMasterId(),
                warehouseParts
        );

        if (reserved == null) {
        	order.setStatus(Order.Status.CANCELLED_BY_MASTER);
        	orderRepository.save(order);
        	orderItemRepository.deleteAll(order.getItems());
        	
        	//добавил
        	if (order.getWarrantyId() != null) {
                supportClient.cancelWarranty(order.getWarrantyId(), false);
            }
        	
            throw new RuntimeException("Warehouse error");
            
        }

        try {
            return saga.finalizeOrder(order, reserved, clientId);
        } catch (Exception e) {

            if (reserved) {
                warehouseClient.cancelReserve(orderId, branchId, order.getMasterId(), warehouseParts);
            }

            throw e;
        }
        
        //return finalizeOrder(order, reserved, clientId);
    }
    
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
    
    @Transactional
    public Order completeOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != Order.Status.IN_PROGRESS) {
            throw new IllegalStateException("Order cannot be completed");
        }
        
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .oldStatus(order.getStatus()) 
                .newStatus(Order.Status.COMPLETED)
                .changedBy(order.getMasterId())
                .changedAt(Instant.now())
                .build();
        historyRepository.save(history);
        
        order.setStatus(Order.Status.COMPLETED);
        
        order.setCompletedAt(Instant.now());
        order.setUpdatedAt(Instant.now());

        return orderRepository.save(order);
    }

    //добавить SAGA 
    @Transactional
    public Order issueOrder(Long orderId, String pickupCode) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getPickupCode().equals(pickupCode)) {
            throw new IllegalArgumentException("Invalid pickup code");
        }

        if ((order.getStatus() != Order.Status.COMPLETED) && (order.getStatus() != Order.Status.CANCELLED_BY_MASTER) && (order.getStatus() != Order.Status.CANCELLED_BY_CLIENT)) {
            throw new IllegalStateException("Order is not completed");
        }
        
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .oldStatus(order.getStatus()) 
                .newStatus(Order.Status.ISSUED)
                .changedBy(order.getMasterId())
                .changedAt(Instant.now())
                .build();
        historyRepository.save(history);

        order.setStatus(Order.Status.ISSUED);
        order.setUpdatedAt(Instant.now());
        
        //добавил
        Order saved = orderRepository.save(order);
        
        //Добавил
        if (order.getWarrantyId() != null) {

            List<OrderItem> items = orderItemRepository.findByOrderOrderId(orderId);

            List<OrderItemRequest> itemsToSupport = items.stream()
                    .map(this::toOrderItemRequest)
                    .toList();

            Boolean completed = supportClient.completeWarranty(
                    order.getWarrantyId(),
                    order.getMasterId(),
                    itemsToSupport
            );

            if (completed == null) {
                throw new RuntimeException("Support service error");
            }
        }

        return saved;
    }
//
//    public Order cancelOrder(Long orderId,
//                             Long branchId,
//                             List<Long> itemsToRemove,
//                             boolean cancelledByClient) {
//
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new RuntimeException("Order not found"));
//
//        List<OrderItem> items = orderItemRepository.findByOrderOrderId(orderId);
//
//        List<OrderItem> removedItems = items.stream()
//                .filter(i -> itemsToRemove.contains(i.getId()))
//                .toList();
//
//        orderItemRepository.deleteAll(removedItems);
//
//        List<OrderItem> remaining = orderItemRepository.findByOrderOrderId(orderId);
//
//        List<OrderItem> parts = remaining.stream()
//                .filter(i -> "PART".equals(i.getItemType()))
//                .toList();
//
//        List<WarehousePartRequest> warehouseParts = parts.stream()
//                .map(this::toWarehousePart)
//                .toList();
//
//        if (!warehouseParts.isEmpty()) {
//            warehouseClient.cancelReserve(orderId, branchId, warehouseParts);
//        }
//
//        order.setStatus(cancelledByClient ?
//                Order.Status.CANCELLED_BY_CLIENT :
//                Order.Status.CANCELLED_BY_MASTER);
//
//        order.setUpdatedAt(Instant.now());
//
//        return orderRepository.save(order);
//    }
//    
//    public Order cancelOrder(Long orderId,
//            Long branchId,
//            List<Long> itemsToRemove,
//            boolean cancelledByClient) {
//
//		Order order = orderRepository.findById(orderId)
//		.orElseThrow(() -> new RuntimeException("Order not found"));
//		
//		List<OrderItem> items = orderItemRepository.findByOrderOrderId(orderId);
//		
//		List<WarehousePartRequest> warehouseParts;
//		
//		switch (order.getStatus()) {
//		
//			case WAITING_FOR_APPROVAL:
//			
//				// просто очищаем items
//				orderItemRepository.deleteAll(items);
//				warehouseParts = List.of();
//				break;
//			
//			
//			case WAITING_FOR_PARTS:
//			
//				// возвращаем все PART
//				warehouseParts = items.stream()
//				       .filter(i -> "PART".equals(i.getItemType()))
//				       .map(this::toWarehousePart)
//				       .toList();
//				break;
//				
//			
//			case IN_PROGRESS:
//				
//				// удаляем использованные items
//				List<OrderItem> removedItems = items.stream()
//				       .filter(i -> itemsToRemove.contains(i.getId()))
//				       .toList();
//				
//				orderItemRepository.deleteAll(removedItems);
//				
////				List<OrderItem> remaining = orderItemRepository.findByOrderOrderId(orderId);
//				
////				warehouseParts = remaining.stream()
////				       .filter(i -> "PART".equals(i.getItemType()))
////				       .map(this::toWarehousePart)
////				       .toList();
//				
//				warehouseParts = removedItems.stream()
//					       .filter(i -> "PART".equals(i.getItemType()))
//					       .map(this::toWarehousePart)
//					       .toList();
//				
//				break;
//				
//			
//			default:
//				throw new IllegalStateException("Order cannot be cancelled in status: " + order.getStatus());
//		}
//				
////		if (!warehouseParts.isEmpty()) {
////			warehouseClient.cancelReserve(orderId, branchId, warehouseParts);
////		}
//		
//		if (!warehouseParts.isEmpty()) {
//			boolean success = warehouseClient.cancelReserve(orderId, branchId, order.getMasterId(),  warehouseParts);
//			if (!success) {
//			    throw new RuntimeException("Warehouse cancel failed");
//			}
//		}
//		
//		OrderStatusHistory history = OrderStatusHistory.builder()
//                .order(order)
//                .oldStatus(order.getStatus()) 
//                .changedBy(order.getMasterId())
//                .changedAt(Instant.now())
//                .build();
//       
//				
//		order.setStatus(cancelledByClient ? Order.Status.CANCELLED_BY_CLIENT: Order.Status.CANCELLED_BY_MASTER);
//		order.setUpdatedAt(Instant.now());
//		
//		history.setNewStatus(order.getStatus());
//		historyRepository.save(history);
//		
//		
//				
//		return orderRepository.save(order);
//	}
    
    
    public Order cancelOrder(
            Long orderId,
            Long branchId,
            List<Long> itemsToRemove,
            boolean cancelledByClient) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<OrderItem> items = orderItemRepository.findByOrderOrderId(orderId);

        List<OrderItem> removedItems = new ArrayList<>();
        List<WarehousePartRequest> warehouseParts = new ArrayList<>();

        switch (order.getStatus()) {

            case WAITING_FOR_APPROVAL:

                removedItems.addAll(items);
                break;

            case WAITING_FOR_PARTS:

                removedItems.addAll(items);

                warehouseParts = items.stream()
                        .filter(i -> "PART".equals(i.getItemType()))
                        .map(this::toWarehousePart)
                        .toList();

                break;

            case IN_PROGRESS:

                removedItems = items.stream()
                        .filter(i -> itemsToRemove.contains(i.getId()))
                        .toList();

                warehouseParts = removedItems.stream()
                        .filter(i -> "PART".equals(i.getItemType()))
                        .map(this::toWarehousePart)
                        .toList();

                break;

            default:
                throw new IllegalStateException(
                        "Order cannot be cancelled in status: " + order.getStatus());
        }

        // warehouse операция
        if (!warehouseParts.isEmpty()) {

            boolean success = warehouseClient.cancelReserve(
                    orderId,
                    branchId,
                    order.getMasterId(),
                    warehouseParts
            );

            if (!success) {
                throw new RuntimeException("Warehouse cancel failed");
            }
        }

        // локальная транзакция
        return saga.finalizeCancel(order, removedItems, cancelledByClient);
    }
    
    @Transactional
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
    
    public boolean hasActiveOrders(Long masterId) {
        return orderRepository.existsByMasterIdAndStatusNot(masterId, Order.Status.ISSUED);
    }
    
    
    @Transactional
    public void setOrdersInProgress(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return;
        }
        
        List<Order> orders = orderRepository.findAllById(orderIds);
        
        for (Order order : orders) {
            if (order.getStatus() != Order.Status.WAITING_FOR_PARTS) {
                throw new IllegalStateException(
                    String.format("Order %d cannot be set to IN_PROGRESS from status: %s", 
                        order.getOrderId(), order.getStatus()));
            }
            
            OrderStatusHistory history = OrderStatusHistory.builder()
                    .order(order)
                    .oldStatus(order.getStatus()) 
                    .newStatus(Order.Status.IN_PROGRESS)
                    .changedBy(order.getMasterId())
                    .changedAt(Instant.now())
                    .build();
            
            order.setStatus(Order.Status.IN_PROGRESS);
            order.setUpdatedAt(Instant.now());
            
            historyRepository.save(history);
        }
        
        orderRepository.saveAll(orders);
    }
    
    @Transactional
    public OrderDetailsDto getOrderWithItems(Long orderId) {

        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        List<OrderItemDto> items = order.getItems()
                .stream()
                .map(item -> OrderItemDto.builder()
                        .id(item.getId())
                        .itemType(item.getItemType())
                        .itemId(item.getItemId())
                        .name(item.getName())
                        .articleNumber(item.getArticleNumber())
                        .category(item.getCategory())
                        .sellPrice(item.getSellPrice())
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        return OrderDetailsDto.builder()
                .orderId(order.getOrderId())
                .clientId(order.getClientId())
                .masterId(order.getMasterId())
                .warrantyId(order.getWarrantyId())
                .status(order.getStatus().name())
                .diagnosticResult(order.getDiagnosticResult())
                .createdAt(order.getCreatedAt())
                .completedAt(order.getCompletedAt())
                .items(items)
                .build();
    }
    
    @Transactional
    public Page<Order> getOrdersByClientIssued(
            Long clientId,
            Pageable pageable) {

        pageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "completedAt")
        );

        return orderRepository.findByClientIdAndStatus(
                clientId,
                Order.Status.ISSUED,
                pageable
        );
    }
    
    @Transactional
    public Page<Order> getOrdersByClientNotIssued(
            Long clientId,
            Pageable pageable) {

        pageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return orderRepository.findByClientIdAndStatusNot(
                clientId,
                Order.Status.ISSUED,
                pageable
        );
    }
    
    @Transactional
    public Page<Order> getOrdersByMasterAndStatus(
            Long masterId,
            Order.Status status,
            Pageable pageable) {

        if (status == Order.Status.ISSUED) {
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "completedAt")
            );
        } else {
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt")
            );
        }

        return orderRepository.findByStatusAndMasterId(status, masterId, pageable);
    }
    
    @Transactional
    public Order getOrderById(
            Long orderId) {

        return orderRepository.findByOrderId(orderId).orElseThrow(() -> new EntityNotFoundException(
                "Order not found"));
    }
    
    @Transactional
    public String getPickupCode(Long orderId) {
    	
    	Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    	
    	return order.getPickupCode();
    }
    
    private OrderItemRequest toOrderItemRequest(OrderItem item) {

        return OrderItemRequest.builder()
                .id(item.getItemId())
                .name(item.getName())
                .serviceCode(item.getArticleNumber())
                .category(item.getCategory())
                .costPrice(item.getCostPrice())
                .sellPrice(item.getSellPrice())
                .masterPercentage(item.getMasterPercentage())
                .quantity(item.getQuantity())
                .build();
    }
//    
//    
//    public List<Order> getOrdersByMasterAndStatus(Long masterId, Order.Status status) {
//        return orderRepository.findByStatusAndMasterId(status, masterId);
//    }
//    
//    public List<Order> getOrdersByClientNotIssued(Long clientId) {
//        return orderRepository.findByClientIdAndStatusNot(clientId, Order.Status.ISSUED);
//    }
//    
//    public List<Order> getOrdersByClientIssued(Long clientId) {
//        return orderRepository.findByClientIdAndStatus(clientId, Order.Status.ISSUED);
//    }
    
}