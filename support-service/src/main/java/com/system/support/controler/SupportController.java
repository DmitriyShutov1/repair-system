package com.system.support.controler;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.system.support.dto.CreateSupportRequestDto;
import com.system.support.dto.SupInfo;
import com.system.support.dto.WarrantyOrderItem;
import com.system.support.entity.SupportRequest;
import com.system.support.entity.SupportRequestStatus;
import com.system.support.service.SupportService;

import lombok.*;

@RestController
@RequestMapping("/api/support-requests")
@RequiredArgsConstructor
public class SupportController {

    private final SupportService supportService;
    
    @PostMapping
    public ResponseEntity<Boolean> createSupportRequest(
            @RequestBody CreateSupportRequestDto dto,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Branch-Id") Long branchId,
            @RequestHeader("X-User-Role") String role
            ) {
    	supportService.createSupportRequest(dto, branchId, userId);
        return ResponseEntity.ok(true);
    }

    @PostMapping("/{id}/require-return")
    public ResponseEntity<SupportRequest> requireReturn(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                supportService.requireReturn(id)
        );
    }

    @PostMapping("/{id}/confirm-return")
    public ResponseEntity<SupportRequest> confirmReturn(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                supportService.confirmReturn(id)
        );
    }

    @PostMapping("/{id}/start-warranty")
    public ResponseEntity<SupportRequest> startWarrantyRepair(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long masterId) {
        return ResponseEntity.ok(
                supportService.startWarrantyRepair(id, masterId)
        );
    }

    @PostMapping("/{id}/cancel-warranty")
    public ResponseEntity<SupportRequest> cancelWarrantyRepair(
            @PathVariable Long id,
            @RequestParam boolean refund) {
        return ResponseEntity.ok(
                supportService.cancelWarrantyRepair(id, refund)
        );
    }

    @PostMapping("/{id}/complete-warranty")
    public ResponseEntity<SupportRequest> completeWarrantyRepair(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long masterId,
            @RequestBody List<WarrantyOrderItem> parts) {
        return ResponseEntity.ok(
                supportService.completeWarrantyRepair(id, masterId, parts)
        );
    }

    
    @GetMapping
    public ResponseEntity<Page<SupportRequest>> getSupportRequests(
    		@RequestHeader("X-User-Id") Long userId,
    		@RequestHeader("X-User-Role") String role,
            @RequestParam SupportRequestStatus status,
            Pageable pageable) {

        return ResponseEntity.ok(
                supportService.findBySupportAndStatus(userId, status, pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupportRequest> getSupportRequest(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                supportService.getSupportRequest(id)
        );
    }
    
    @GetMapping("/getTicketsToMaster")
    public Page<SupportRequest> getWarrantyQueue(
    		@RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Branch-Id") Long branchId,
            @RequestHeader("X-User-Role") String role,
            Pageable pageable) {

        return supportService.findByBranchAndStatus(branchId, pageable);
    }
    
    @GetMapping("/getTicketsToClient")
    public Page<SupportRequest> getClientOrders(
    		@RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @RequestParam SupportRequestStatus status,
            Pageable pageable) {

        return supportService.findByClientAndStatus(userId, status, pageable);
    }
    
    @PostMapping("/warrantyNeeded/{id}")
    public ResponseEntity<SupportRequest> setWarranty(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                supportService.setNeedWarrantyRepair(id)
        );
    }
    
    @GetMapping("/listForOrder/{id}")
    public List<SupportRequest> findByOrderId(
            @PathVariable("id") Long orderId) {

        return supportService.findReqByOrder(orderId);
    }
    
    @GetMapping("/{id}/info")
    public ResponseEntity<SupInfo> getSupportInfo(@PathVariable Long id) {

        return ResponseEntity.ok(
                supportService.getSupportInfo(id)
        );
    }
}