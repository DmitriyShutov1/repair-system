//package com.system.orders.integration;
//
//import com.system.orders.dto.*;
//import com.system.orders.entity.Order;
//import com.system.orders.repository.OrderRepository;
//import com.system.orders.service.OrderItemService;
//import com.system.orders.service.OrderService;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
//@Transactional
//class OrderIntegrationTest {
//
//    @Autowired private OrderService orderService;
//    @Autowired private OrderItemService orderItemService;
//    @Autowired private OrderRepository orderRepository;
//
//    @Test
//    void fullFlow_create_and_setItems() {
//        CreateOrderRequest request = new CreateOrderRequest();
//        request.setClientId(1L);
//
//        Order order = orderService.createOrder(request, 10L, 20L, "MASTER");
//
//        SetOrderItemsRequest itemsRequest = new SetOrderItemsRequest();
//        itemsRequest.setOrderId(order.getOrderId());
//        itemsRequest.setItems(List.of(
//                OrderItemRequest.builder()
//                        .id(1L)
//                        .itemType("PART")
//                        .name("Test")
//                        .serviceCode("A1")
//                        .category("cat")
//                        .costPrice(BigDecimal.TEN)
//                        .sellPrice(BigDecimal.TEN)
//                        .masterPercentage(BigDecimal.TEN)
//                        .quantity(1)
//                        .build()
//        ));
//
//        orderItemService.setOrderItems(itemsRequest);
//
//        Order updated = orderRepository.findById(order.getOrderId()).get();
//
//        assertEquals(Order.Status.WAITING_FOR_APPROVAL, updated.getStatus());
//    }
//}