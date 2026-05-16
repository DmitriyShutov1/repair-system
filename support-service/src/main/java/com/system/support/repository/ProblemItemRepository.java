package com.system.support.repository;

import com.system.support.entity.ProblemItem;
import com.system.support.entity.SupportRequest;
import com.system.support.entity.SupportRequestStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemItemRepository extends JpaRepository<ProblemItem, Long> {
    List<ProblemItem> findBySupportRequest(SupportRequest supportRequest);

    List<ProblemItem> findBySupportRequestId(Long supportRequestId);
    
}