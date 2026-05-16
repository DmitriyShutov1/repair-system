package com.system.orders.repository;

import com.system.orders.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findTop50ByProcessedFalseOrderByCreatedAtAsc();
}