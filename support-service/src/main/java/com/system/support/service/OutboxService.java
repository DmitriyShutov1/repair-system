package com.system.support.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system.support.dto.OperationEventDTO;
import com.system.support.entity.OutboxEvent;
import com.system.support.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxRepo;
    private final ObjectMapper objectMapper;

    public void saveEvent(OperationEventDTO event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outbox = OutboxEvent.builder()
                    .eventId(event.getEventId())
                    .eventType(event.getType().name())
                    .payload(payload)
                    .build();

            outboxRepo.save(outbox);

        } catch (Exception e) {
            throw new RuntimeException("Outbox save error", e);
        }
    }
}