package com.system.warehouse.service;

import com.system.warehouse.entity.Part;
import com.system.warehouse.entity.PartWaitingList;
import com.system.warehouse.repository.PartWaitingListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PartWaitingListService {

    private final PartWaitingListRepository repository;

    /**
     * Добавление записи в список ожидания
     */
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


    /**
     * Удаление записи по id
     */
    @Transactional
    public void deleteById(Long id) {
        repository.setThisClosed(id);
    }


    /**
     * Изменение количества
     */
//    @Transactional
//    public void updateQuantity(Long id, Integer quantity) {
//        int updated = repository.updateQuantity(id, quantity);
//
//        if (updated == 0) {
//            throw new RuntimeException("Waiting list entry not found: " + id);
//        }
//    }
    
    @Transactional
    public void updateQuantity(Long id, Integer quantity) {
    	PartWaitingList waitingList = repository.findById(id)
    	        .orElseThrow(() -> new RuntimeException("Waiting list entry not found: " + id));
    	waitingList.setRequiredQuantity(quantity);
    }


    /**
     * Поиск по id
     */
    @Transactional(readOnly = true)
    public PartWaitingList getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Waiting list entry not found: " + id));
    }


    /**
     * Поиск по part_id и branch_id
     */
    @Transactional(readOnly = true)
    public List<PartWaitingList> getByPartAndBranch(Long partId, Long branchId) {
        return repository.findActiveByPartIdAndBranchId(partId, branchId);
    }


    /**
     * Агрегация количества по order_id
     */
    @Transactional(readOnly = true)
    public Integer getTotalRequiredQuantityByOrder(Long orderId) {
        return repository.sumRequiredQuantityByOrderId(orderId);
    }
}