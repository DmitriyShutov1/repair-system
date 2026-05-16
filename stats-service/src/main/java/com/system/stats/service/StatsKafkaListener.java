
package com.system.stats.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system.stats.dto.OperationEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatsKafkaListener {

    private final ObjectMapper objectMapper;
    private final StatsProcessingService statsProcessingService;

    @KafkaListener(topics = "operation-events", groupId = "stats-group")
    public void listen(String message, Acknowledgment ack) {
        try {
            OperationEventDto event = objectMapper.readValue(message, OperationEventDto.class);

            log.info(" Received event: {}", event.getEventId());

            statsProcessingService.processEvent(event);

            ack.acknowledge();

        } catch (Exception e) {
            log.error("Kafka processing error", e);
        }
    }
}