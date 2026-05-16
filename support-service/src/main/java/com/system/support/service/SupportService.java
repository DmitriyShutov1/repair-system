package com.system.support.service;

import com.system.support.dto.*;
import com.system.support.entity.*;
import com.system.support.kafka.StatsEventProducer;
import com.system.support.repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupportService {

    private final SupportRequestRepository supportRequestRepository;
    private final ProblemItemRepository problemItemRepository;
    private final OutboxService outboxService;

    @Transactional
    public SupportRequest createSupportRequest(CreateSupportRequestDto dto, Long branchId, Long supportId) {

    	
    	List<SupportRequest> active = supportRequestRepository.findByOrderIdAndStatusNotIn(
    		    dto.getOrderId(), 
    		    List.of(SupportRequestStatus.RETURNED, SupportRequestStatus.WARRANTY_REPAIR_COMPLETED)
    		);
    	
    	if(!active.isEmpty()) {
    		throw new IllegalStateException("Обращение по заказу уже создано и активно");
    	}
    	
        SupportRequest request = SupportRequest.builder()
                .supportId(supportId)
                .branchId(branchId)
                .orderId(dto.getOrderId())
                .masterId(dto.getMasterId())
                .description(dto.getDescription())
                .deviceSerial(dto.getDeviceSerial())   
                .deviceModel(dto.getDeviceModel())      
                .clientId(dto.getClientId())
                .status(SupportRequestStatus.CREATED)
                .build();
        
        List<ProblemItem> items = new ArrayList<>();

        if (dto.getItems() != null) {
            dto.getItems().forEach(itemDto -> {
                ProblemItem item = ProblemItem.builder()
                        .supportRequest(request)
                        .itemType(itemDto.getItemType())
                        .name(itemDto.getName())
                        .category(itemDto.getCategory())
                        .sellPrice(itemDto.getSellPrice())
                        .quantity(itemDto.getQuantity())
                        .build();
                items.add(item);
            });
        }

        request.setItems(items);

        return supportRequestRepository.save(request);
    }
    
    @Transactional
    public SupportRequest requireReturn(Long supportRequestId) {

        SupportRequest request = supportRequestRepository.findById(supportRequestId)
                .orElseThrow();

        if (request.getStatus() != SupportRequestStatus.CREATED) {
            throw new IllegalStateException("Return not allowed in current status");
        }

        BigDecimal refund = request.getItems().stream()
                .map(i -> i.getSellPrice()
                        .multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        request.setRefundCost(refund);
        request.setStatus(SupportRequestStatus.RETURN_REQUIRED);

        return supportRequestRepository.save(request);
    }
    
    @Transactional
    public SupportRequest confirmReturn(Long supportRequestId) {

        SupportRequest request = supportRequestRepository.findById(supportRequestId)
                .orElseThrow();

        if (request.getStatus() != SupportRequestStatus.RETURN_REQUIRED) {
            throw new IllegalStateException("Invalid status");
        }

        request.setStatus(SupportRequestStatus.RETURNED);
        request.setCompletedAt(Instant.now());
        
        SupportRequest saved = supportRequestRepository.save(request);
        
        outboxService.saveEvent(OperationEventDTO.builder()
                .eventId(UUID.randomUUID())
                .type(OperationEventDTO.OperationType.REFUND)
                .eventTime(LocalDateTime.now())
                .branchId(saved.getBranchId())
                .masterId(saved.getMasterId())
                .supportId(saved.getSupportId())
                .orderId(saved.getOrderId())
                .clientAmount(saved.getRefundCost())
                .build());

        return saved;
    }
    
    @Transactional
    public SupportRequest setNeedWarrantyRepair(Long supportRequestId) {

        SupportRequest request = supportRequestRepository.findById(supportRequestId)
                .orElseThrow();

        if (request.getStatus() != SupportRequestStatus.CREATED) {
            throw new IllegalStateException("Invalid status");
        }

        request.setStatus(SupportRequestStatus.WARRANTY_REPAIR_REQUIRED);

        return supportRequestRepository.save(request);
    }
    
    
    @Transactional
    public SupportRequest startWarrantyRepair(Long supportRequestId, Long masterId) {

        SupportRequest request = supportRequestRepository.findById(supportRequestId)
                .orElseThrow();
        
        if (request.getStatus() == SupportRequestStatus.WARRANTY_REPAIR_IN_PROGRESS) {
            return request;
        }

        if (request.getStatus() != SupportRequestStatus.WARRANTY_REPAIR_REQUIRED) {
            throw new IllegalStateException("Invalid status");
        }

        request.setCompletedByMasterId(masterId);
        request.setStatus(SupportRequestStatus.WARRANTY_REPAIR_IN_PROGRESS);

        return supportRequestRepository.save(request);
    }
    
    @Transactional
    public SupportRequest cancelWarrantyRepair(Long supportRequestId, boolean refund) {

        SupportRequest request = supportRequestRepository.findById(supportRequestId)
                .orElseThrow();

        if (refund) {
        	request.setStatus(SupportRequestStatus.CREATED);
            return requireReturn(supportRequestId);
        }

        request.setStatus(SupportRequestStatus.WARRANTY_REPAIR_REQUIRED);

        return supportRequestRepository.save(request);
    }
    
    @Transactional
    public SupportRequest completeWarrantyRepair(
            Long supportRequestId,
            Long masterId,
            List<WarrantyOrderItem> parts) {

        SupportRequest request = supportRequestRepository.findById(supportRequestId)
                .orElseThrow();
        
        if (request.getStatus() == SupportRequestStatus.WARRANTY_REPAIR_COMPLETED) {
            return request;
        }

        BigDecimal masterCost = BigDecimal.ZERO;
        BigDecimal cost = BigDecimal.ZERO;

        for (WarrantyOrderItem part : parts) {

            BigDecimal partMaster = part.getSellPrice()
                    .multiply(part.getMasterPercentage())
                    .divide(BigDecimal.valueOf(100));

            masterCost = masterCost.add(
                    partMaster.multiply(BigDecimal.valueOf(part.getQuantity()))
            );

            cost = cost.add(
                    part.getCostPrice().multiply(BigDecimal.valueOf(part.getQuantity()))
            );
        }

        request.setCompletedByMasterId(masterId);
        request.setMasterCost(masterCost);
        request.setCost(cost);
        request.setStatus(SupportRequestStatus.WARRANTY_REPAIR_COMPLETED);
        request.setCompletedAt(Instant.now());
        
        SupportRequest saved = supportRequestRepository.save(request);
        
        outboxService.saveEvent(OperationEventDTO.builder()
                .eventId(UUID.randomUUID())
                .type(OperationEventDTO.OperationType.WARRANTY_COMPLETED)
                .eventTime(LocalDateTime.now())
                .branchId(saved.getBranchId())
                .masterId(masterId)
                .supportId(saved.getSupportId())
                .orderId(saved.getOrderId())
                .costPrice(saved.getCost())
                .masterAmount(saved.getMasterCost())
                .build());

        return supportRequestRepository.save(request);
    }
    
    @Transactional(readOnly = true)
    public Page<SupportRequest> findBySupportAndStatus(
            Long supportId,
            SupportRequestStatus status,
            Pageable pageable) {

        return supportRequestRepository
                .findBySupportIdAndStatus(supportId, status, pageable);
    }
    
    @Transactional(readOnly = true)
    public SupportRequest getSupportRequest(Long id) {

        return supportRequestRepository.findById(id)
                .orElseThrow();
    }
    
    @Transactional(readOnly = true)
    public Page<SupportRequest> findByBranchAndStatus(Long branchId, Pageable pageable) {

        return supportRequestRepository.findByBranchIdAndStatus(branchId, SupportRequestStatus.WARRANTY_REPAIR_REQUIRED, pageable);
                
    }
    
    @Transactional(readOnly = true)
    public Page<SupportRequest> findByClientAndStatus(Long clientId, SupportRequestStatus status, Pageable pageable) {

        return supportRequestRepository.findByClientIdAndStatus(clientId, status, pageable);
                
    }
    
    
    @Transactional(readOnly = true)
    public SupInfo getSupportInfo(Long supportRequestId) {

        SupportRequest request = supportRequestRepository.findById(supportRequestId)
                .orElseThrow(() -> new RuntimeException("Support request not found"));

        List<ItemInfo> items = request.getItems().stream()
                .map(this::toItemInfo)
                .toList();

        return SupInfo.builder()
                .id(request.getId())
                .masterId(request.getMasterId())
                .branchId(request.getBranchId())
                .clientId(request.getClientId())
                .status(request.getStatus())
                .orderId(request.getOrderId())
                .description(request.getDescription())
                .deviceSerial(request.getDeviceSerial())   
                .deviceModel(request.getDeviceModel())      
                .refundCost(request.getRefundCost())
                .createdAt(request.getCreatedAt())
                .completedAt(request.getCompletedAt())
                .items(items)
                .build();
    }
    
    public List<SupportRequest> findReqByOrder(Long orderId){
    	return supportRequestRepository.findByOrderId(orderId);
    }
    
    private ItemInfo toItemInfo(ProblemItem item) {

        return ItemInfo.builder()
                .id(item.getId())
                .itemType(item.getItemType())
                .name(item.getName())
                .category(item.getCategory())
                .sellPrice(item.getSellPrice())
                .quantity(item.getQuantity())
                .createdAt(item.getCreatedAt())
                .build();
    }
}