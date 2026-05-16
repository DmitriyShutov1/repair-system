package com.system.warehouse.repository;

import com.system.warehouse.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findTop50ByProcessedFalseOrderByCreatedAtAsc();
}