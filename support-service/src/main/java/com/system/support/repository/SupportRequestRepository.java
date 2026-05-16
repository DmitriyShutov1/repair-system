package com.system.support.repository;

import com.system.support.entity.SupportRequest;
import com.system.support.entity.SupportRequestStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportRequestRepository extends JpaRepository<SupportRequest, Long> {

    List<SupportRequest> findByOrderId(Long orderId);
    
    List<SupportRequest> findByOrderIdAndStatusNotIn(Long orderId, List<SupportRequestStatus> statuses);

    Page<SupportRequest> findBySupportIdAndStatus(
            Long supportId,
            SupportRequestStatus status,
            Pageable pageable
    );
    
    Page<SupportRequest> findByBranchIdAndStatus(
            Long branchId,
            SupportRequestStatus status,
            Pageable pageable
    );
    
    Page<SupportRequest> findByClientIdAndStatus(
            Long clientId,
            SupportRequestStatus status,
            Pageable pageable
    );
}