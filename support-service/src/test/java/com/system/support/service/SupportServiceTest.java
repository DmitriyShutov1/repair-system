package com.system.support.service;

import com.system.support.dto.CreateSupportRequestDto;
import com.system.support.dto.OperationEventDTO;
import com.system.support.dto.WarrantyOrderItem;
import com.system.support.entity.*;
import com.system.support.repository.ProblemItemRepository;
import com.system.support.repository.SupportRequestRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportServiceTest {

    @Mock
    private SupportRequestRepository supportRequestRepository;

    @Mock
    private ProblemItemRepository problemItemRepository;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private SupportService supportService;

    private SupportRequest request;

    @BeforeEach
    void setup() {

        request = SupportRequest.builder()
                .id(1L)
                .branchId(1L)
                .clientId(20L)
                .masterId(10L)
                .supportId(30L)
                .orderId(100L)
                .status(SupportRequestStatus.CREATED)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void requireReturn_success() {

        ProblemItem item = ProblemItem.builder()
                .sellPrice(BigDecimal.valueOf(100))
                .quantity(2)
                .build();

        request.setItems(List.of(item));

        when(supportRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        when(supportRequestRepository.save(any(SupportRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SupportRequest result = supportService.requireReturn(1L);

        assertEquals(
                SupportRequestStatus.RETURN_REQUIRED,
                result.getStatus()
        );

        assertEquals(
                BigDecimal.valueOf(200),
                result.getRefundCost()
        );

        verify(supportRequestRepository).save(request);
    }

    @Test
    void requireReturn_wrongStatus() {

        request.setStatus(SupportRequestStatus.RETURNED);

        when(supportRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> supportService.requireReturn(1L)
        );

        assertEquals(
                "Return not allowed in current status",
                ex.getMessage()
        );

        verify(supportRequestRepository, never()).save(any());
    }

    @Test
    void confirmReturn_success() {

        request.setStatus(SupportRequestStatus.RETURN_REQUIRED);

        when(supportRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        when(supportRequestRepository.save(any(SupportRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SupportRequest result =
                supportService.confirmReturn(1L);

        assertEquals(
                SupportRequestStatus.RETURNED,
                result.getStatus()
        );

        assertNotNull(result.getCompletedAt());

        verify(outboxService).saveEvent(any(OperationEventDTO.class));
    }

    @Test
    void startWarrantyRepair_success() {

        request.setStatus(
                SupportRequestStatus.WARRANTY_REPAIR_REQUIRED
        );

        when(supportRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        when(supportRequestRepository.save(any(SupportRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SupportRequest result =
                supportService.startWarrantyRepair(1L, 55L);

        assertEquals(
                SupportRequestStatus.WARRANTY_REPAIR_IN_PROGRESS,
                result.getStatus()
        );

        assertEquals(55L, result.getCompletedByMasterId());
    }

    @Test
    void completeWarrantyRepair_success() {

        request.setStatus(
                SupportRequestStatus.WARRANTY_REPAIR_IN_PROGRESS
        );

        WarrantyOrderItem item = WarrantyOrderItem.builder()
                .costPrice(BigDecimal.valueOf(50))
                .sellPrice(BigDecimal.valueOf(100))
                .masterPercentage(BigDecimal.valueOf(50))
                .quantity(2)
                .build();

        when(supportRequestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        when(supportRequestRepository.save(any(SupportRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SupportRequest result =
                supportService.completeWarrantyRepair(
                        1L,
                        10L,
                        List.of(item)
                );

        assertEquals(
                SupportRequestStatus.WARRANTY_REPAIR_COMPLETED,
                result.getStatus()
        );

        assertEquals(
                BigDecimal.valueOf(100),
                result.getCost()
        );

        verify(outboxService).saveEvent(any(OperationEventDTO.class));
    }
}