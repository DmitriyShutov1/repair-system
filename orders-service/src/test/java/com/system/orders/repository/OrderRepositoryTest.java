package com.system.orders.repository;

import com.system.orders.entity.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public RestTemplateBuilder restTemplateBuilder() {
            return new RestTemplateBuilder();
        }
    }

    @Test
    void saveOrder_success() {
        Order order = Order.builder()
                .clientId(20L)
                .masterId(10L)
                .branchId(1L)
                .status(Order.Status.CREATED)
                .pickupCode("12345678")
                .createdAt(Instant.now())
                .build();

        Order saved = orderRepository.save(order);
        assertNotNull(saved.getOrderId());
        
        Order found = orderRepository.findById(saved.getOrderId()).orElse(null);
        assertNotNull(found);
        assertEquals(20L, found.getClientId());
        assertEquals(Order.Status.CREATED, found.getStatus());
    }

    @Test
    void existsByMasterIdAndStatusNot_success() {
        Order order = Order.builder()
                .clientId(20L)
                .masterId(10L)
                .branchId(1L)
                .status(Order.Status.IN_PROGRESS)
                .pickupCode("12345678")
                .createdAt(Instant.now())
                .build();

        orderRepository.save(order);

        boolean exists = orderRepository.existsByMasterIdAndStatusNot(
                10L,
                Order.Status.ISSUED
        );
        assertTrue(exists);
    }
}