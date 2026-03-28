package com.system.support.repository;

import com.system.support.entity.ProblemItem;
import com.system.support.entity.SupportRequest;
import com.system.support.entity.SupportRequestStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemItemRepository extends JpaRepository<ProblemItem, Long> {

    // все проблемные позиции для обращения
    List<ProblemItem> findBySupportRequest(SupportRequest supportRequest);

    // часто удобнее искать сразу по id
    List<ProblemItem> findBySupportRequestId(Long supportRequestId);
    
}