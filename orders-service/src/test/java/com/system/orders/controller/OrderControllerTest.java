

package com.system.orders.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import static org.mockito.Mockito.doThrow;
import com.system.orders.entity.Order;
import com.system.orders.service.OrderService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrdersControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;
    
    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private com.system.orders.service.OrderItemService orderItemService;

    @Test
    void completeOrder_success() throws Exception {

        Order order = Order.builder()
                .orderId(1L)
                .clientId(20L)
                .masterId(10L)
                .status(Order.Status.COMPLETED)
                .createdAt(Instant.now())
                .build();

        when(orderService.completeOrder(1L))
                .thenReturn(order);

        mockMvc.perform(
                post("/api/orders/1/complete")
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "MASTER")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void issueOrder_success() throws Exception {

        Order order = Order.builder()
                .orderId(1L)
                .status(Order.Status.ISSUED)
                .pickupCode("12345678")
                .createdAt(Instant.now())
                .build();

        when(orderService.issueOrder(1L, "12345678"))
                .thenReturn(order);

        mockMvc.perform(
                post("/api/orders/1/issue")
                        .param("pickupCode", "12345678")
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "MASTER")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ISSUED"));
    }
    
    @Test
    void issueOrder_invalidPickupCode() throws Exception {

        when(orderService.issueOrder(1L, "WRONG"))
                .thenThrow(new IllegalArgumentException("Invalid pickup code"));

        mockMvc.perform(
                post("/api/orders/1/issue")
                        .param("pickupCode", "WRONG")
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "MASTER")
        )
        .andExpect(status().isBadRequest()); 
        
    }
    
    @Test
    void completeOrder_wrongStatus() throws Exception {

        when(orderService.completeOrder(1L))
                .thenThrow(new IllegalStateException("Order cannot be completed"));

        mockMvc.perform(
                post("/api/orders/1/complete")
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "MASTER")
        )
        .andExpect(status().isConflict()); 
        
    }
}