package com.system.orders.service;

import com.system.orders.client.SupportClient;
import com.system.orders.client.UsersClient;
import com.system.orders.client.WarehouseClient;
import com.system.orders.dto.TestResultDto;
import com.system.orders.entity.Order;
import com.system.orders.entity.OrderStatusHistory;
import com.system.orders.entity.TestSession;
import com.system.orders.entity.TestSessionStep;
import com.system.orders.repository.*;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderStatusHistoryRepository historyRepository;

    @Mock
    private TestRepository testRepository;

    @Mock
    private TestSessionRepository testSessionRepository;

    @Mock
    private TestSessionStepRepository testSessionStepRepository;

    @Mock
    private SupportClient supportClient;

    @Mock
    private WarehouseClient warehouseClient;

    @Mock
    private UsersClient usersClient;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private OrderService orderService;

    private Order order;

    @BeforeEach
    void setup() {
        order = Order.builder()
                .orderId(1L)
                .masterId(10L)
                .clientId(20L)
                .status(Order.Status.IN_PROGRESS)
                .pickupCode("12345678")
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void completeOrder_success() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.completeOrder(1L);

        assertEquals(Order.Status.COMPLETED, result.getStatus());
        assertNotNull(result.getCompletedAt());

        verify(historyRepository, times(1))
                .save(any(OrderStatusHistory.class));

        verify(orderRepository, times(1))
                .save(order);
    }

    @Test
    void completeOrder_wrongStatus() {

        order.setStatus(Order.Status.CREATED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> orderService.completeOrder(1L)
        );

        assertEquals("Order cannot be completed", ex.getMessage());

        verify(orderRepository, never()).save(any());
    }

    @Test
    void issueOrder_invalidPickupCode() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.issueOrder(1L, "WRONG")
        );

        assertEquals("Invalid pickup code", ex.getMessage());

        verify(orderRepository, never()).save(any());
    }

    @Test
    void saveTestResults_allPassed() {

    	com.system.orders.entity.Test test1 = com.system.orders.entity.Test.builder()
                .id(1L)
                .active(true)
                .build();

    	com.system.orders.entity.Test test2 = com.system.orders.entity.Test.builder()
                .id(2L)
                .active(true)
                .build();

        TestResultDto dto1 = TestResultDto.builder()
                .testId(1L)
                .passed(true)
                .build();

        TestResultDto dto2 = TestResultDto.builder()
                .testId(2L)
                .passed(true)
                .build();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(testRepository.findByActiveTrue())
                .thenReturn(List.of(test1, test2));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        orderService.saveTestResults(
                1L,
                10L,
                List.of(dto1, dto2)
        );

        assertEquals(Order.Status.COMPLETED, order.getStatus());

        verify(testSessionRepository, times(1))
                .save(any(TestSession.class));

        verify(testSessionStepRepository, times(2))
                .save(any(TestSessionStep.class));

        verify(historyRepository, times(1))
                .save(any(OrderStatusHistory.class));

        verify(orderRepository, times(1))
                .save(order);
    }
    
    
    @Test
    void confirmOrder_success_inProgress() {

        order.setStatus(Order.Status.WAITING_FOR_APPROVAL);
        order.setBranchId(1L);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderItemRepository.findByOrderOrderId(1L))
                .thenReturn(List.of());

        when(warehouseClient.reserveParts(
                anyLong(),
                anyLong(),
                anyLong(),
                anyList()
        )).thenReturn(false);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.confirmOrder(1L, 20L);

        assertEquals(Order.Status.IN_PROGRESS, result.getStatus());
        assertTrue(result.getClientApproved());

        verify(historyRepository).save(any(OrderStatusHistory.class));
        verify(orderRepository).save(order);
    }
    
    @Test
    void confirmOrder_wrongStatus() {

        order.setStatus(Order.Status.CREATED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> orderService.confirmOrder(1L, 20L)
        );

        assertEquals("Order not waiting for approval", ex.getMessage());

        verify(orderRepository, never()).save(any());
    }
    
    @Test
    void saveTestResults_notAllTestsProvided() {

        com.system.orders.entity.Test test1 = com.system.orders.entity.Test.builder()
                .id(1L)
                .active(true)
                .build();

        com.system.orders.entity.Test test2 = com.system.orders.entity.Test.builder()
                .id(2L)
                .active(true)
                .build();

        TestResultDto dto1 = TestResultDto.builder()
                .testId(1L)
                .passed(true)
                .build();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(testRepository.findByActiveTrue())
                .thenReturn(List.of(test1, test2));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> orderService.saveTestResults(
                        1L,
                        10L,
                        List.of(dto1)
                )
        );

        assertEquals("Not all active tests were provided", ex.getMessage());

        verify(testSessionRepository, never()).save(any());
    }
    
    @Test
    void issueOrder_success() {

        order.setStatus(Order.Status.COMPLETED);
        order.setBranchId(1L);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderItemRepository.findByOrderOrderId(1L))
                .thenReturn(List.of());

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.issueOrder(1L, "12345678");

        assertEquals(Order.Status.ISSUED, result.getStatus());

        verify(historyRepository).save(any(OrderStatusHistory.class));
        verify(orderRepository).save(order);
    }
}