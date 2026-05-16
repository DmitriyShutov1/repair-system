package com.system.orders.controller;

import com.system.orders.dto.CreateOrderRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.system.orders.dto.OrderBranchPair;
import com.system.orders.dto.OrderDetailsDto;
import com.system.orders.dto.OrderResponse;
import com.system.orders.dto.SetOrderItemsRequest;
import com.system.orders.dto.TestDto;
import com.system.orders.dto.TestResultDto;
import com.system.orders.dto.TestSessionDto;
import com.system.orders.dto.TestSessionStepDto;
import com.system.orders.entity.Order;
import com.system.orders.service.OrderItemService;
import com.system.orders.service.OrderService;
import com.system.orders.service.PdfActService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
//
//@RestController
//@RequestMapping("/api/orders")
//@RequiredArgsConstructor
//public class OrderController {
//
//    private final OrderService orderService;
//    private final OrderItemService orderItemService;
//
//
//    @PostMapping
//    public Order createOrder(@RequestBody CreateOrderRequest request) {
//        return orderService.createOrder(request);
//    }
//    
////    @PostMapping
////    public Order createOrder(@RequestBody CreateOrderRequest request, 
////    		@RequestHeader("X-User-Id") Long masterId,
////            @RequestHeader("X-Branch-Id") Long branchId) {
////    	request.setMasterId(masterId);
////        return orderService.createOrder(request);
////    }
//
////
////    @PostMapping("/items")
////    public void setItems(@RequestHeader("X-User-Id") Long masterId, @RequestHeader("X-Branch-Id") Long branchId, @RequestBody SetOrderItemsRequest request) {
////    	request.setBranchId(branchId);
////        orderItemService.setOrderItems(request);
////    }
////    
//    
//    @PostMapping("/items")
//    public void setItems(@RequestBody SetOrderItemsRequest request) {
//        orderItemService.setOrderItems(request);
//    }
//
//
//    @PostMapping("/{orderId}/confirm")
//    public Order confirmOrder(@PathVariable Long orderId,
//                              @RequestParam Long branchId) {
//
//        return orderService.confirmOrder(orderId, branchId);
//    }
//
//
//    @PostMapping("/{orderId}/complete")
//    public Order completeOrder(@PathVariable Long orderId) {
//        return orderService.completeOrder(orderId);
//    }
//
//
//    @PostMapping("/{orderId}/issue")
//    public Order issueOrder(@PathVariable Long orderId,
//                            @RequestParam String pickupCode) {
//
//        return orderService.issueOrder(orderId, pickupCode);
//    }
//
//
//    @PostMapping("/{orderId}/cancel")
//    public Order cancelOrder(@PathVariable Long orderId,
//                             @RequestParam Long branchId,
//                             @RequestParam boolean cancelledByClient,
//                             @RequestBody List<Long> itemsToRemove) {
//
//        return orderService.cancelOrder(
//                orderId,
//                branchId,
//                itemsToRemove,
//                cancelledByClient
//        );
//    }
//
//}
import java.util.Map;


@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final PdfActService pdfActService;

    
    @PostMapping
    public OrderResponse createOrder(
            @RequestBody CreateOrderRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Branch-Id") Long branchId,
            @RequestHeader("X-User-Role") String role
    ) {
        return mapToResponse(orderService.createOrder(request, userId, branchId, role));
    }


    @PostMapping("/items")
    public Map<String, String> setItems(
            @RequestBody SetOrderItemsRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Branch-Id") Long branchId,
            @RequestHeader("X-User-Role") String role
    ) {
        orderItemService.setOrderItems(request);
        return Map.of("status", "ok");
    }


    @PostMapping("/{orderId}/confirm")
    public OrderResponse confirmOrder(
            @PathVariable Long orderId,
            @RequestHeader("X-User-Id") Long userId,
           // @RequestHeader("X-Branch-Id") Long branchId,
            @RequestHeader("X-User-Role") String role
    ) {
        return mapToResponse(orderService.confirmOrder(orderId, userId));
    }


    @PostMapping("/{orderId}/complete")
    public OrderResponse completeOrder(@PathVariable Long orderId, @RequestHeader("X-User-Id") Long userId, @RequestHeader("X-User-Role") String role) {

        return mapToResponse(orderService.completeOrder(orderId));
    }


    @PostMapping("/{orderId}/issue")
    public OrderResponse issueOrder(
            @PathVariable Long orderId,
            @RequestParam String pickupCode, 
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role
    ) {

        return mapToResponse(orderService.issueOrder(orderId, pickupCode));
    }


    @PostMapping("/{orderId}/cancel")
    public OrderResponse cancelOrder(
            @PathVariable Long orderId,
            @RequestHeader("X-Branch-Id") Long branchId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @RequestParam boolean cancelledByClient,
            @RequestBody List<Long> itemsToRemove
    ) {

        return mapToResponse(orderService.cancelOrder(
                orderId,
                branchId,
                itemsToRemove,
                cancelledByClient
        ));
    }
    
    
    
    @GetMapping("/master/{masterId}/has-active-orders")
    public boolean hasActiveOrders(@PathVariable Long masterId) {
        return orderService.hasActiveOrders(masterId);
    }
    
    
    @PostMapping("/set-in-progress")
    public void setOrdersInProgress(@RequestBody List<Long> orderIds) {
        orderService.setOrdersInProgress(orderIds);
    }
    
    @PostMapping("/cancel-orders")
    public void cancelOrders(@RequestBody List<OrderBranchPair> pairs) {
        if (pairs == null || pairs.isEmpty()) {
            return;
        }
        
        for (OrderBranchPair pair : pairs) {
            try {
                orderService.cancelOrder(
                    pair.getOrderId(), 
                    pair.getBranchId(), 
                    List.of(), 
                    false
                );
            } catch (Exception e) {
                System.err.println("Failed to cancel order " + pair.getOrderId() + 
                                 " for branch " + pair.getBranchId() + ": " + e.getMessage());
            }
        }
    }
    
    @PostMapping("/master/my-orders")
    public Page<OrderResponse> getMasterOrders(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role, 
            @RequestParam Order.Status status,
            Pageable pageable
    ) {
    	Page<Order> ordersPage = orderService.getOrdersByMasterAndStatus(userId, status, pageable);
        return ordersPage.map(this::mapToResponse);
    }
    
    @PostMapping("/client/my-active-orders")
    public Page<OrderResponse> getClientActiveOrders(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            Pageable pageable
    ) {
    	Page<Order> ordersPage = orderService.getOrdersByClientNotIssued(userId, pageable);
        return ordersPage.map(this::mapToResponse);
        
    }
    
    @PostMapping("/client/my-issued-orders")
    public Page<OrderResponse> getClientIssuedOrders(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            Pageable pageable
    ) {
    	Page<Order> ordersPage = orderService.getOrdersByClientIssued(userId, pageable);
        return ordersPage.map(this::mapToResponse);
    }
    
    @PostMapping("/master/getOrderById")
    public OrderResponse getOrderById(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @RequestParam Long orderId
    ) {
        return mapToResponse(orderService.getOrderById(orderId));
    }
    
    @PostMapping("/info")
    public OrderDetailsDto getOrderIndo(
            @RequestHeader("X-User-Role") String role, 
            @RequestParam Long orderId
    ) {
        return orderService.getOrderWithItems(orderId);
    }
    
    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .clientId(order.getClientId())
                .masterId(order.getMasterId())
                .warrantyId(order.getWarrantyId())
                .status(order.getStatus())
                .diagnosticResult(order.getDiagnosticResult())
                .deviceSerial(order.getDeviceSerial())   
                .deviceModel(order.getDeviceModel())     
                .clientApproved(order.getClientApproved())
                .pickupCode(order.getPickupCode())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .completedAt(order.getCompletedAt())
                .build();
    }
    
    
    @PostMapping("/pickup-code")
    public Map<String, String> getPickUpCode(
            @RequestHeader("X-User-Role") String role, 
            @RequestParam Long orderId
    ) {
    	return Map.of("pickupCode", orderService.getPickupCode(orderId));
    }
    
    @DeleteMapping("/{orderId}/items")
    public Map<String, String> removeItems(
            @PathVariable Long orderId,
            @RequestBody List<Long> itemIds,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Branch-Id") Long branchId,
            @RequestHeader("X-User-Role") String role
    ) {
        orderItemService.removeOrderItems(orderId, itemIds, userId, branchId);
        return Map.of("status", "ok");
    }
    
    
    @GetMapping("/tests")
    public List<TestDto> getTests(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role
    ) {
        return orderService.getAllTests();
    }
    
    
    @PostMapping("/{orderId}/tests/results")
    public Map<String, String> saveTestResults(
            @PathVariable Long orderId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @RequestBody List<TestResultDto> results
    ) {
        orderService.saveTestResults(orderId, userId, results);
        return Map.of("status", "ok");
    }
    
    
    @GetMapping("/{orderId}/tests/sessions")
    public List<TestSessionDto> getTestSessions(
            @PathVariable Long orderId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role
    ) {
        return orderService.getSessionsByOrder(orderId);
    }

    
    @GetMapping("/tests/sessions/{sessionId}/steps")
    public List<TestSessionStepDto> getTestSessionSteps(
            @PathVariable Long sessionId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role
    ) {
        return orderService.getStepsBySession(sessionId);
    }
    
    @GetMapping("/{orderId}/repair-act")
    public ResponseEntity<byte[]> downloadRepairAct(
            @PathVariable Long orderId
    ) {

        byte[] pdf = pdfActService.generateRepairAct(orderId);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=repair-act-" + orderId + ".pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}