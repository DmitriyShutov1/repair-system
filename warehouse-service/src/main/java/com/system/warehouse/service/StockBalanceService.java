package com.system.warehouse.service;

import com.system.warehouse.client.OrdersClient;
import com.system.warehouse.dto.PartResponse;
import com.system.warehouse.dto.PartWithPriceAndStockDto;
import com.system.warehouse.entity.*;
import com.system.warehouse.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StockBalanceService {
	private final StockBalanceRepository repository;
    private final PartRepository partRepository; 
    private final PartWaitingListRepository waitingListRepository;
    private final StockMovementRepository movementRepository;
    private final OrdersClient ordersClient;
    
    @Transactional
    public StockBalance createStock(Long partId, Long branchId, Integer initialQuantity) {
    	Part part = partRepository.findById(partId)
    	        .orElseThrow(() -> new RuntimeException("Part not found: " + partId));
        
        StockBalance stock = StockBalance.builder()
            .part(part)
            .branchId(branchId)
            .quantity(initialQuantity)
            .build();
        
        return repository.save(stock);
    }
    
//    
//    @Transactional
//    public void increaseQuantity(Long partId, Long branchId, Integer amount) {
//    	
//    	Optional<StockBalance> stock1 = repository.findByPartIdAndBranchId(partId, branchId);
//        StockBalance stock = stock1.get();
//        stock.setQuantity(stock.getQuantity() + amount);
//    }
    
//    @Transactional
//    public void increaseQuantity(Long partId, Long branchId, Integer amount) {
//    	
//    	Optional<StockBalance> stockOpt = repository.findByPartIdAndBranchId(partId, branchId);
//    	
//    	if (stockOpt.isPresent()) {
//    	    // Запись есть - увеличиваем
//    	    StockBalance stock = stockOpt.get();
//    	    stock.setQuantity(stock.getQuantity() + amount);
//    	} else {
//    	    // Записи нет - создаем новую с amount как начальное количество
//    	    createStock(partId, branchId, amount);
//    	}
//    }
//    
//    @Transactional
//    public void decreaseQuantity(Long partId, Long branchId, Integer amount) {
//        Optional<StockBalance> stock1 = repository.findByPartIdAndBranchId(partId, branchId);
//        StockBalance stock = stock1.get();
//        
//        if (stock.getQuantity() < amount) {
//            throw new RuntimeException(
//                String.format("Insufficient stock. Available: %d, requested: %d", 
//                    stock.getQuantity(), amount)
//            );
//        }
//        
//        stock.setQuantity(stock.getQuantity() - amount);
//    }
//    
    @Transactional
    public void deleteAllByBranch(Long branchId, Long userId) {
        repository.deleteAllByBranchId(branchId);
        //удаление всей истории аудита для бранча
    }

    @Transactional
    public void deleteAllByPart(Long partId) {
        repository.deleteAllByPartId(partId);
    }
    
    
    @Transactional
    public void receiveParts(String articleNumber, Long branchId, Integer receivedQuantity, Long userId) {
    	
    	Part part = partRepository.findByArticleNumber(articleNumber)
    	        .orElseThrow(() -> new RuntimeException("Part not found with article: " + articleNumber));
    	
    	Long partId = part.getId();
        
        // 1. Получаем или создаем запись о наличии
        StockBalance stock = repository.findByPartIdAndBranchId(partId, branchId)
            .orElseGet(() -> createStock(partId, branchId, 0));
        
        // 2. Получаем активные записи из листа ожидания для этой запчасти и филиала
        List<PartWaitingList> waitingEntries = waitingListRepository
            .findActiveByPartIdAndBranchId(partId, branchId);
        
        int remainingQuantity = receivedQuantity;
        
        Set<Long> readyOrders = new HashSet<>();
        
        // 3. Проходим по каждой записи в листе ожидания
        for (PartWaitingList entry : waitingEntries) {
            if (remainingQuantity <= 0) {
                break; // детали закончились
            }
            
            int requiredQuantity = entry.getRequiredQuantity();
            Long orderId = entry.getOrderId();
            
            if (remainingQuantity >= requiredQuantity) {
                // Можем полностью закрыть запрос
                entry.setClosed(true); // закрываем запись
                
                StockMovement movement = StockMovement.builder()
                        .part(part)
                        .branchId(branchId)
                        .masterId(userId)
                        .movementType(MovementType.WAITING_LIST_FULFILLED)
                        .reason("Поступление от поставщика")
                        .quantity(requiredQuantity)
                        .orderId(entry.getOrderId())
                        .build();
                movementRepository.save(movement);
                remainingQuantity -= requiredQuantity;
                
                // Проверяем, остались ли еще детали для этого заказа
                Integer stillNeeded = waitingListRepository
                    .sumRequiredQuantityByOrderId(orderId);
                
                if (stillNeeded == 0) {
                    System.out.println("Заказ " + orderId + " полностью обеспечен деталями");
                    
                    readyOrders.add(orderId);
                    // TODO: здесь будет вызов в сервис заказов
                    
                    
                } else {
                    System.out.println("Заказ " + orderId + " еще ожидает " + stillNeeded + " деталей");
                }
                
            } else {
                // Частично покрываем запрос
                entry.setRequiredQuantity(requiredQuantity - remainingQuantity);
                
                StockMovement movement = StockMovement.builder()
                        .part(part)
                        .branchId(branchId)
                        .masterId(userId)
                        .movementType(MovementType.IN_WAITING_LIST)
                        .reason("Поступление от поставщика")
                        .quantity(remainingQuantity)
                        .orderId(entry.getOrderId())
                        .build();
                movementRepository.save(movement);
                remainingQuantity = 0;
                // Заказ все еще ждет остаток
                System.out.println("Заказ " + orderId + " частично обеспечен, осталось " + 
                		entry.getRequiredQuantity() + " деталей");
            }
        }
        
        // 4. Оставшиеся детали добавляем на склад
        if (remainingQuantity > 0) {
            stock.setQuantity(stock.getQuantity() + remainingQuantity);
            System.out.println("Остаток " + remainingQuantity + " деталей добавлен на склад");
            StockMovement movement = StockMovement.builder()
                    .part(part)
                    .branchId(branchId)
                    .masterId(userId)
                    .movementType(MovementType.IN_STOCK)
                    .reason("Поступление от поставщика")
                    .quantity(remainingQuantity)
                    .orderId(null)
                    .build();
            movementRepository.save(movement);

        }
                if (!readyOrders.isEmpty()) {
            ordersClient.notifyOrdersInProgress(readyOrders);
        }
    }
    

    
    @Transactional
    public void cancelOrder(Long orderId, Long branchId, List<PartWithPriceAndStockDto> items, Long masterId) {
        
    	List<StockMovement> move = movementRepository.findByOrderIdAndMovementType(orderId, MovementType.RETURN_TO_STOCK);
        if(!move.isEmpty()) {
        	return;
        }
        
        for (PartWithPriceAndStockDto item : items) {
            Long partId = item.getId();
            Integer orderedQuantity = item.getQuantity();
            
            // 1. Ищем запись в листе ожидания для этого заказа
            Optional<PartWaitingList> waitingOpt = waitingListRepository.findByOrderIdAndPartIdAndBranchIdAndClosedFalse(orderId, partId, branchId);
            
            
            int waitingQuantity = 0;
            
            // 2. Если есть запись в листе ожидания - закрываем
            if (waitingOpt.isPresent()) {
                PartWaitingList waiting = waitingOpt.get();
                waitingQuantity = waiting.getRequiredQuantity();
                waiting.setClosed(true);
            }
            
            // 3. Остаток (заказано минус то что было в листе) кладем на склад
            int remainder = orderedQuantity - waitingQuantity;
            
            if (remainder > 0) {
                StockBalance stock = repository.findByPartIdAndBranchId(partId, branchId)
                    .orElseGet(() -> createStock(partId, branchId, 0));
                
                stock.setQuantity(stock.getQuantity() + remainder);

                StockMovement movement = StockMovement.builder()
                        .part(partRepository.findById(partId).orElseThrow(() -> new RuntimeException("part not found in canceling order")))
                        .branchId(branchId)
                        .masterId(masterId)
                        .movementType(MovementType.RETURN_TO_STOCK)
                        .reason("Поступление от поставщика")
                        .quantity(remainder)
                        .orderId(orderId)
                        .build();
                movementRepository.save(movement);
            }
        }
    }
    
    

    @Transactional
    public boolean createOrder(Long orderId, Long branchId, List<PartWithPriceAndStockDto> items, Long masterId) {
    	boolean waitingParts = false;
        
        for (PartWithPriceAndStockDto item : items) {
            Long partId = item.getId();
            Integer requestedQuantity = item.getQuantity();
            
            // 1. Проверяем наличие на складе
            Optional<StockBalance> stockOpt = repository.findByPartIdAndBranchId(partId, branchId);
            
            if (stockOpt.isPresent()) {
                StockBalance stock = stockOpt.get();
                
                if (stock.getQuantity() >= requestedQuantity) {
                    // Случай 1: Хватает - списываем всё со склада
                    stock.setQuantity(stock.getQuantity() - requestedQuantity);
                    StockMovement movement = StockMovement.builder()
                            .part(partRepository.findById(partId).orElseThrow(() -> new RuntimeException("part not found in canceling order")))
                            .branchId(branchId)
                            .masterId(masterId)
                            .movementType(MovementType.OUT_TO_ORDER)
                            .reason("Поступление от поставщика")
                            .quantity(requestedQuantity)
                            .orderId(orderId)
                            .build();
                    movementRepository.save(movement);
                    
                } else {
                    // Не хватает - проверяем активна ли запчасть
                    Part part = partRepository.findById(partId).orElseThrow(() -> new RuntimeException("Part not found: " + partId));
                    
                    if (!part.isActive()) {
                        throw new RuntimeException(String.format("Insufficient stock for inactive part: %d. Available: %d, requested: %d", 
                        		partId, stock.getQuantity(), requestedQuantity));
                    }
                    
                    // Случай 2: Запчасть активна - списываем что есть, остальное в лист ожидания
                    int available = stock.getQuantity();
                    int deficit = requestedQuantity - available;
                    
                    StockMovement movement = StockMovement.builder()
                            .part(part)
                            .branchId(branchId)
                            .masterId(masterId)
                            .movementType(MovementType.OUT_TO_ORDER)
                            .reason("Поступление от поставщика")
                            .quantity(available)
                            .orderId(orderId)
                            .build();
                    movementRepository.save(movement);
                    
                    stock.setQuantity(0); // списываем все что есть

                    
                    PartWaitingList waitingEntry = PartWaitingList.builder()
                    	.orderId(orderId)
                    	.part(part)
                    	.branchId(branchId)
                    	.requiredQuantity(deficit)
                    	.build();
                    waitingListRepository.save(waitingEntry);
                    waitingParts = true;
                }
            } else {
            	throw new RuntimeException(String.format("Balance not found: %d. BranchId: %d", partId, branchId));
            }
            
        }
        return waitingParts;
    }
    
    
    @Transactional
    public void removeParts(String articleNumber, Long branchId, Integer quantityToRemove, Long userId) {

        Part part = partRepository.findByArticleNumber(articleNumber)
                .orElseThrow(() -> new RuntimeException(
                        "Part not found with article: " + articleNumber));

        StockBalance stock = repository.findByPartIdAndBranchId(part.getId(), branchId)
                .orElseThrow(() -> new RuntimeException(
                        "Stock balance not found for article " + articleNumber +
                        " in branch " + branchId));

        if (stock.getQuantity() < quantityToRemove) {
            throw new RuntimeException(
                    String.format(
                            "Insufficient stock for article %s. Available: %d, requested: %d",
                            articleNumber,
                            stock.getQuantity(),
                            quantityToRemove
                    )
            );
        }

        stock.setQuantity(stock.getQuantity() - quantityToRemove);

		StockMovement movement = StockMovement.builder()
		        .part(part)
		        .branchId(branchId)
		        .masterId(userId)
		        .movementType(MovementType.OUT_LOST)
		        .reason("Списание")
		        .quantity(quantityToRemove)
		        .orderId(null)
		        .build();
		movementRepository.save(movement);
    }
}