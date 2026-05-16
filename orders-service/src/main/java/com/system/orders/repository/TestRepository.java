package com.system.orders.repository;

import com.system.orders.entity.OrderStatusHistory;
import com.system.orders.entity.Test;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface TestRepository extends JpaRepository<Test, Long> {
    List<Test> findByActiveTrue();
}
