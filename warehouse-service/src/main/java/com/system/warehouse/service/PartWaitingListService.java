package com.system.warehouse.service;

import com.system.warehouse.client.OrdersClient;
import com.system.warehouse.dto.OrderBranchPair;
import com.system.warehouse.entity.Part;
import com.system.warehouse.entity.PartWaitingList;
import com.system.warehouse.repository.PartRepository;
import com.system.warehouse.repository.PartWaitingListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartWaitingListService {

    private final PartWaitingListRepository repository;
    private final PartRepository partRepository;
    private final OrdersClient ordersClient;

    

    @Transactional
    public PartWaitingList addToWaitingList(Long orderId,
                                            Part part,
                                            Long branchId,
                                            Integer quantity) {

        PartWaitingList entity = PartWaitingList.builder()
                .orderId(orderId)
                .part(part)
                .branchId(branchId)
                .requiredQuantity(quantity)
                .build();

        return repository.save(entity);
    }



    @Transactional
    public void deleteById(Long id) {
        repository.setThisClosed(id);
    }


    @Transactional
    public void updateQuantity(Long id, Integer quantity) {
    	PartWaitingList waitingList = repository.findById(id)
    	        .orElseThrow(() -> new RuntimeException("Waiting list entry not found: " + id));
    	waitingList.setRequiredQuantity(quantity);
    }


    @Transactional(readOnly = true)
    public PartWaitingList getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Waiting list entry not found: " + id));
    }


    @Transactional(readOnly = true)
    public List<PartWaitingList> getByPartAndBranch(Long partId, Long branchId) {
        return repository.findActiveByPartIdAndBranchId(partId, branchId);
    }


    @Transactional(readOnly = true)
    public Integer getTotalRequiredQuantityByOrder(Long orderId) {
        return repository.sumRequiredQuantityByOrderId(orderId);
    }
    
    
    @Transactional
    public void closeAllByPartId(Long partId) {
    	Part part = partRepository.findById(partId)
    	        .orElseThrow(() -> new RuntimeException("Part not found: " + partId));
    	if(part.isActive()) {
    		throw new RuntimeException("Part is active");
    	}
        // Получаем список активных записей для запчасти
        List<PartWaitingList> activeEntries = repository.findActiveByPartId(partId);
        
        if (activeEntries.isEmpty()) {
            return;
        }
        
     // Собираем уникальные пары orderId + branchId
        List<OrderBranchPair> pairs = activeEntries.stream()
            .map(entry -> new OrderBranchPair(entry.getOrderId(), entry.getBranchId()))
            .distinct()  // чтобы один заказ не дублировался если ждет несколько деталей
            .collect(Collectors.toList());
        
        for (PartWaitingList entry : activeEntries) {
            entry.setClosed(true);
        }
        // Отправляем запрос на отмену заказов через клиент
        ordersClient.cancelOrders(pairs);
    }
}