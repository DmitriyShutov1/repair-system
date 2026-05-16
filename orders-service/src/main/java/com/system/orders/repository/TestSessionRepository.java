package com.system.orders.repository;

import com.system.orders.entity.OrderStatusHistory;
import com.system.orders.entity.Test;
import com.system.orders.entity.TestSession;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface TestSessionRepository extends JpaRepository<TestSession, Long> {
    List<TestSession> findByOrderId(Long orderId);
}
