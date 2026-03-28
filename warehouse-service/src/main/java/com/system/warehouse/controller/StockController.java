package com.system.warehouse.controller;

import com.system.warehouse.dto.PartWithPriceAndStockDto;
import com.system.warehouse.dto.StockReservationResponse;
import com.system.warehouse.service.PartWaitingListService;
import com.system.warehouse.service.StockBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockBalanceService stockService;
    private final PartWaitingListService waitingService;
    

    /**
     * Создание заказа (резервирование запчастей)
     */
    @PostMapping("/order/create")
    public StockReservationResponse createOrder(
            @RequestParam Long orderId,
            @RequestParam Long branchId,
            @RequestParam Long masterId,
            @RequestBody List<PartWithPriceAndStockDto> items
    ) {
        Boolean waiting = stockService.createOrder(orderId, branchId, items, masterId);
        return new StockReservationResponse(waiting);
    }

    /**
     * Отмена заказа
     */
    @PostMapping("/order/cancel")
    public void cancelOrder(
            @RequestParam Long orderId,
            @RequestParam Long branchId,
            @RequestParam Long masterId,
            @RequestBody List<PartWithPriceAndStockDto> items
    ) {
        stockService.cancelOrder(orderId, branchId, items, masterId);
    }

    /**
     * Пополнение склада
     */
    @PostMapping("/receive")
    public Map<String, String> receiveParts(
    		@RequestHeader("X-Branch-Id") Long branchId,
    		@RequestHeader("X-User-Role") String role,
    		@RequestHeader("X-User-Id") Long userId,
            @RequestParam String articleNumber,
            @RequestParam Integer quantity
    ) {
    	if (!"MASTER".equals(role)) 
    	    throw new IllegalStateException("You are not master");
    	
        stockService.receiveParts(articleNumber, branchId, quantity, userId);
        return Map.of("status", "ok");
    }
    
    
    @DeleteMapping("/branch/{branchId}")
    public void deleteAllByBranch(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long branchId
    ) {
        if (!"ADMIN".equals(role)) {
            throw new IllegalStateException("You are not admin");
        }
        stockService.deleteAllByBranch(branchId, userId);
    }
    
    @DeleteMapping("/waitingClear/{partId}")
    public void deleteAllWaitingsByPart(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long partId
    ) {
        if (!"ADMIN".equals(role)) {
            throw new IllegalStateException("You are not admin");
        }
        waitingService.closeAllByPartId(partId);
    }
    
    @PostMapping("/lost")
    public Map<String, String> deleteAllWaitingsByPart(
            @RequestHeader("X-User-Role") String role,
            @RequestParam String articleNumber,
            @RequestParam Integer removeIt,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Branch-Id") Long branchId
            
    ) {
        if (!"MASTER".equals(role)) {
            throw new IllegalStateException("You are not master");
        }
        stockService.removeParts(articleNumber, branchId, removeIt, userId);
        return Map.of("status", "ok");
    }
    
}