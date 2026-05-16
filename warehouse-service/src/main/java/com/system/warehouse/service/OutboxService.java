package com.system.warehouse.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system.warehouse.dto.*;
import com.system.warehouse.entity.*;
import com.system.warehouse.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository repo;
    private final ObjectMapper objectMapper;

    public void save(OperationEventDTO event) {

        try {

            String payload = objectMapper.writeValueAsString(event);

            repo.save(
                    OutboxEvent.builder()
                            .eventId(event.getEventId())
                            .eventType(event.getType().name())
                            .payload(payload)
                            .build()
            );

        } catch (Exception e) {
            throw new RuntimeException("Outbox error", e);
        }
    }
}