package com.system.warehouse.service;

import com.system.warehouse.client.OrdersClient;
import com.system.warehouse.dto.PartWithPriceAndStockDto;
import com.system.warehouse.entity.*;
import com.system.warehouse.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockBalanceServiceTest {

    @Mock
    private StockBalanceRepository repository;

    @Mock
    private PartRepository partRepository;

    @Mock
    private PartWaitingListRepository waitingListRepository;

    @Mock
    private StockMovementRepository movementRepository;

    @Mock
    private PricingPolicyRepository priceRepository;

    @Mock
    private OrdersClient ordersClient;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private StockBalanceService stockService;

    private Part part;
    private StockBalance stock;

    @BeforeEach
    void setup() {

        part = Part.builder()
                .id(1L)
                .name("SSD")
                .articleNumber("SSD-001")
                .active(true)
                .build();

        stock = StockBalance.builder()
                .id(1L)
                .part(part)
                .branchId(1L)
                .quantity(10)
                .build();
    }

    @Test
    void removeParts_success() {

        when(partRepository.findByArticleNumber("SSD-001"))
                .thenReturn(Optional.of(part));

        when(repository.findByPartIdAndBranchId(1L, 1L))
                .thenReturn(Optional.of(stock));

        stockService.removeParts(
                "SSD-001",
                1L,
                3,
                10L
        );

        assertEquals(7, stock.getQuantity());

        verify(movementRepository).save(any(StockMovement.class));
    }

    @Test
    void removeParts_insufficientStock() {

        when(partRepository.findByArticleNumber("SSD-001"))
                .thenReturn(Optional.of(part));

        when(repository.findByPartIdAndBranchId(1L, 1L))
                .thenReturn(Optional.of(stock));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> stockService.removeParts(
                        "SSD-001",
                        1L,
                        50,
                        10L
                )
        );

        assertTrue(ex.getMessage().contains("Insufficient stock"));

        verify(movementRepository, never()).save(any());
    }

    @Test
    void createOrder_enoughStock() {

        PartWithPriceAndStockDto item =
                PartWithPriceAndStockDto.builder()
                        .id(1L)
                        .quantity(4)
                        .build();

        when(movementRepository
                .sumQuantityByOrderIdAndPartIdAndMovementType(
                        anyLong(),
                        anyLong(),
                        any()
                ))
                .thenReturn(0);

        when(repository.findByPartIdAndBranchId(1L, 1L))
                .thenReturn(Optional.of(stock));

        when(partRepository.findById(1L))
                .thenReturn(Optional.of(part));

        boolean waiting = stockService.createOrder(
                1L,
                1L,
                List.of(item),
                10L
        );

        assertFalse(waiting);
        assertEquals(6, stock.getQuantity());

        verify(movementRepository).save(any(StockMovement.class));
    }

    @Test
    void createOrder_balanceNotFound() {

        PartWithPriceAndStockDto item =
                PartWithPriceAndStockDto.builder()
                        .id(1L)
                        .quantity(4)
                        .build();

        when(movementRepository
                .sumQuantityByOrderIdAndPartIdAndMovementType(
                        anyLong(),
                        anyLong(),
                        any()
                ))
                .thenReturn(0);

        when(repository.findByPartIdAndBranchId(1L, 1L))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> stockService.createOrder(
                        1L,
                        1L,
                        List.of(item),
                        10L
                )
        );

        assertTrue(ex.getMessage().contains("Balance not found"));
    }
}