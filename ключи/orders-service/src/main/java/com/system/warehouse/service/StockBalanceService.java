package com.system.warehouse.service;

import com.system.warehouse.entity.Part;
import com.system.warehouse.dto.PartResponse;
import com.system.warehouse.entity.*;
import com.system.warehouse.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StockBalanceService {
	private final StockBalanceRepository repository;
    private final PartRepository partRepository; 
    
    
    @Transactional
    public StockBalance createStock(Long partId, Long branchId, Integer initialQuantity) {
    	Optional<Part> part1 = partRepository.findById(partId); 
    	
    	Part part = part1.get();
        
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
    
    @Transactional
    public void increaseQuantity(Long partId, Long branchId, Integer amount) {
    	
    	Optional<StockBalance> stockOpt = repository.findByPartIdAndBranchId(partId, branchId);
    	
    	if (stockOpt.isPresent()) {
    	    // Запись есть - увеличиваем
    	    StockBalance stock = stockOpt.get();
    	    stock.setQuantity(stock.getQuantity() + amount);
    	} else {
    	    // Записи нет - создаем новую с amount как начальное количество
    	    createStock(partId, branchId, amount);
    	}
    }
    
    @Transactional
    public void decreaseQuantity(Long partId, Long branchId, Integer amount) {
        Optional<StockBalance> stock1 = repository.findByPartIdAndBranchId(partId, branchId);
        StockBalance stock = stock1.get();
        
        if (stock.getQuantity() < amount) {
            throw new RuntimeException(
                String.format("Insufficient stock. Available: %d, requested: %d", 
                    stock.getQuantity(), amount)
            );
        }
        
        stock.setQuantity(stock.getQuantity() - amount);
    }
    
    @Transactional
    public void deleteAllByBranch(Long branchId) {
        repository.deleteAllByBranchId(branchId);
    }

    /**
     * Удалить все остатки по partId
     */
    @Transactional
    public void deleteAllByPart(Long partId) {
        repository.deleteAllByPartId(partId);
    }
}