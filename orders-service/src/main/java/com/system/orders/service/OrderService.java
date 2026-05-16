package com.system.orders.service;

import com.system.orders.client.*;
import com.system.orders.dto.CreateOrderRequest;
import com.system.orders.dto.LatestTestResultDto;
import com.system.orders.dto.OperationEventDTO;
import com.system.orders.dto.OrderDetailsDto;
import com.system.orders.dto.OrderItemDto;
import com.system.orders.dto.OrderItemRequest;
import com.system.orders.dto.TestDto;
import com.system.orders.dto.*;
import com.system.orders.dto.WarehousePartRequest;
import com.system.orders.entity.Order;
import com.system.orders.entity.OrderItem;
import com.system.orders.entity.OrderStatusHistory;
import com.system.orders.entity.Test;
import com.system.orders.entity.TestSession;
import com.system.orders.entity.TestSessionStep;
//import com.system.orders.entity.TestsDescription;
//import com.system.orders.entity.TestsHistory;
import com.system.orders.repository.OrderItemRepository;
import com.system.orders.repository.*;
import com.system.orders.repository.OrderRepository;
import com.system.orders.repository.OrderStatusHistoryRepository;
//import com.system.orders.repository.TestsDescriptionRepository;
//import com.system.orders.repository.TestsHistoryRepository;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository historyRepository;
    
    private final TestRepository testRepository;
    private final TestSessionRepository testSessionRepository;
    private final TestSessionStepRepository testSessionStepRepository;
    
    private final SupportClient supportClient;
    
    private final OutboxService outboxService;
    
    
    private final WarehouseClient warehouseClient;
    private final UsersClient usersClient;
    
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
                .deviceSerial(request.getDeviceSerial())    
                .deviceModel(request.getDeviceModel())     
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
            supportClient.startWarranty(order.getWarrantyId(), userId);
        }
        
        return order;
    }

    private String generatePickupCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
   
    @Transactional
    public Order confirmOrder(Long orderId, Long clientId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (order.getStatus() != Order.Status.WAITING_FOR_APPROVAL) {
            throw new IllegalStateException("Order not waiting for approval");
        }

        List<OrderItem> items = orderItemRepository.findByOrderOrderId(orderId);

        List<WarehousePartRequest> warehouseParts = items.stream()
                .filter(i -> "PART".equals(i.getItemType()))
                .map(this::toWarehousePart)
                .toList();

        boolean hasWaitingParts;

        try {
            hasWaitingParts = warehouseClient.reserveParts(
                    orderId,
                    order.getBranchId(),
                    order.getMasterId(),
                    warehouseParts
            );

        } catch (HttpClientErrorException e) {

            if (order.getWarrantyId() != null) {
                supportClient.cancelWarranty(order.getWarrantyId(), true);
            }

            Order.Status oldStatus = order.getStatus();

            order.setStatus(Order.Status.CANCELLED_BY_MASTER);
            order.setUpdatedAt(Instant.now());

            OrderStatusHistory history = OrderStatusHistory.builder()
                    .order(order)
                    .oldStatus(oldStatus)
                    .newStatus(order.getStatus())
                    .changedBy(order.getMasterId())
                    .changedAt(Instant.now())
                    .build();

            historyRepository.save(history);

            return orderRepository.save(order);

        } catch (HttpServerErrorException e) {

            
            throw new RuntimeException("Warehouse internal error", e);

        } catch (ResourceAccessException e) {

            throw new RuntimeException("Warehouse unavailable", e);
        }catch (CallNotPermittedException e) {
            throw new RuntimeException("Warehouse temporarily unavailable (circuit open)", e);
        }

        order.setClientApproved(true);

        order.setStatus(
                hasWaitingParts
                        ? Order.Status.WAITING_FOR_PARTS
                        : Order.Status.IN_PROGRESS
        );

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
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

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
    
    @Transactional
    public Order issueOrder(Long orderId, String pickupCode) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (!order.getPickupCode().equals(pickupCode)) {
            throw new IllegalArgumentException("Invalid pickup code");
        }

        Order.Status previousStatus = order.getStatus();

        if (previousStatus != Order.Status.COMPLETED &&
            previousStatus != Order.Status.CANCELLED_BY_MASTER &&
            previousStatus != Order.Status.CANCELLED_BY_CLIENT) {
            throw new IllegalStateException("Order is not issuable");
        }

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .oldStatus(previousStatus)
                .newStatus(Order.Status.ISSUED)
                .changedBy(order.getMasterId())
                .changedAt(Instant.now())
                .build();

        historyRepository.save(history);

        order.setStatus(Order.Status.ISSUED);
        order.setUpdatedAt(Instant.now());

        Order saved = orderRepository.save(order);

        if (order.getWarrantyId() == null) {

            OperationEventDTO.OperationType type = switch (previousStatus) {
                case COMPLETED -> OperationEventDTO.OperationType.ORDER_COMPLETED;
                case CANCELLED_BY_CLIENT -> OperationEventDTO.OperationType.ORDER_CANCELLED_CLIENT;
                case CANCELLED_BY_MASTER -> OperationEventDTO.OperationType.ORDER_CANCELLED_MASTER;
                default -> throw new IllegalStateException("Unexpected status");
            };

            List<OrderItem> items = orderItemRepository.findByOrderOrderId(orderId);

            BigDecimal clientAmount = items.stream()
                    .map(i -> i.getSellPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal costPrice = items.stream()
                    .map(i -> i.getCostPrice() == null ? BigDecimal.ZERO :
                            i.getCostPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal masterAmount = items.stream()
                    .map(i -> {
                        if (i.getMasterPercentage() == null) return BigDecimal.ZERO;

                        return i.getSellPrice()
                                .multiply(i.getMasterPercentage())
                                .divide(BigDecimal.valueOf(100))
                                .multiply(BigDecimal.valueOf(i.getQuantity()));
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            outboxService.save(OperationEventDTO.builder()
                    .eventId(UUID.randomUUID())
                    .type(type)
                    .eventTime(LocalDateTime.now())
                    .branchId(order.getBranchId())
                    .masterId(order.getMasterId())
                    .orderId(order.getOrderId())
                    .clientAmount(clientAmount)
                    .costPrice(costPrice)
                    .masterAmount(masterAmount)
                    .build());
        }

        if (order.getWarrantyId() != null) {

            List<OrderItem> items = orderItemRepository.findByOrderOrderId(orderId);

            List<OrderItemRequest> itemsToSupport = items.stream()
                    .map(this::toOrderItemRequest)
                    .toList();

            if (order.getWarrantyId() != null) {
                supportClient.completeWarranty(
                        order.getWarrantyId(),
                        order.getMasterId(),
                        itemsToSupport
                );
            }
        }
        return saved;
    }
    
    @Transactional
    public Order cancelOrder(
            Long orderId,
            Long branchId,
            List<Long> itemsToRemove,
            boolean cancelledByClient) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (order.getStatus() == Order.Status.CANCELLED_BY_CLIENT ||
            order.getStatus() == Order.Status.CANCELLED_BY_MASTER) {
            return order;
        }

        List<OrderItem> items = orderItemRepository.findByOrderOrderId(orderId);

        List<OrderItem> removedItems;
        List<WarehousePartRequest> warehouseParts;

        switch (order.getStatus()) {

            case WAITING_FOR_APPROVAL:
                removedItems = items;
                warehouseParts = items.stream()
                        .filter(i -> "PART".equals(i.getItemType()))
                        .map(this::toWarehousePart)
                        .toList();
                break;

            case WAITING_FOR_PARTS:
                removedItems = items;
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

        if (!warehouseParts.isEmpty()) {
            warehouseClient.cancelReserve(
                    orderId,
                    branchId,
                    order.getMasterId(),
                    warehouseParts
            );
        }

        if (!removedItems.isEmpty()) {
            orderItemRepository.deleteAll(removedItems);
        }
        
        Order.Status oldStatus = order.getStatus();

        order.setStatus(
                cancelledByClient
                        ? Order.Status.CANCELLED_BY_CLIENT
                        : Order.Status.CANCELLED_BY_MASTER
        );

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .oldStatus(oldStatus)
                .newStatus(order.getStatus())
                .changedBy(order.getMasterId())
                .changedAt(Instant.now())
                .build();

        historyRepository.save(history);

        order.setUpdatedAt(Instant.now());

        return orderRepository.save(order);
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
        	if (order.getStatus() == Order.Status.IN_PROGRESS) {
                continue;
            }
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
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

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
                .deviceSerial(order.getDeviceSerial())     
                .deviceModel(order.getDeviceModel())       
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
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
    	
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
    
 
    @Transactional
    public List<TestDto> getAllTests() {
        return testRepository.findByActiveTrue()
                .stream()
                .map(t -> TestDto.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .description(t.getDescription())
                        .build())
                .toList();
    }
    
    @Transactional
    public void saveTestResults(Long orderId, Long masterId, List<TestResultDto> results) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (order.getStatus() != Order.Status.IN_PROGRESS &&
            order.getStatus() != Order.Status.TESTING) {
            throw new IllegalStateException("Order is not ready for testing");
        }

        List<Test> activeTests = testRepository.findByActiveTrue();

        List<Long> activeIds = activeTests.stream()
                .map(Test::getId)
                .toList();

        List<Long> incomingIds = results.stream()
                .map(TestResultDto::getTestId)
                .toList();

        if (!incomingIds.containsAll(activeIds)) {
            throw new IllegalStateException("Not all active tests were provided");
        }

        TestSession session = TestSession.builder()
                .orderId(orderId)
                .build();

        testSessionRepository.save(session);

        boolean allPassed = true;

        for (TestResultDto dto : results) {

            TestSessionStep step = TestSessionStep.builder()
                    .sessionId(session.getId())
                    .testId(dto.getTestId())
                    .passed(dto.isPassed())
                    .build();

            testSessionStepRepository.save(step);

            if (!dto.isPassed()) {
                allPassed = false;
            }
        }

        Order.Status oldStatus = order.getStatus();
        Order.Status newStatus = allPassed
                ? Order.Status.COMPLETED
                : Order.Status.IN_PROGRESS;

        order.setStatus(newStatus);
        order.setUpdatedAt(Instant.now());

        if (newStatus == Order.Status.COMPLETED) {
            order.setCompletedAt(Instant.now());
        }

        orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedBy(masterId)
                .changedAt(Instant.now())
                .build();

        historyRepository.save(history);
    }
    
    @Transactional
    public List<TestSessionDto> getSessionsByOrder(Long orderId) {

        return testSessionRepository.findByOrderId(orderId)
                .stream()
                .sorted(Comparator.comparing(TestSession::getSessionAt).reversed())
                .map(s -> TestSessionDto.builder()
                        .sessionId(s.getId())
                        .sessionAt(s.getSessionAt())
                        .build())
                .toList();
    }
    
    @Transactional
    public List<TestSessionStepDto> getStepsBySession(Long sessionId) {

        return testSessionStepRepository.findBySessionId(sessionId)
                .stream()
                .map(s -> TestSessionStepDto.builder()
                        .stepId(s.getId())
                        .testId(s.getTestId())
                        .passed(s.getPassed())
                        .createdAt(s.getCreatedAt())
                        .build())
                .toList();
    }

}


