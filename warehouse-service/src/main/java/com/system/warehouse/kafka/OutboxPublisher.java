package com.system.warehouse.kafka;

import com.system.warehouse.entity.*;
import com.system.warehouse.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository repo;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "operation-events";

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publish() {

        List<OutboxEvent> events =
                repo.findTop50ByProcessedFalseOrderByCreatedAtAsc();

        for (OutboxEvent e : events) {

            try {

                kafkaTemplate.send(TOPIC, e.getPayload()).get();

                e.setProcessed(true);
                e.setProcessedAt(LocalDateTime.now());

            } catch (Exception ex) {

                log.error("Kafka send failed {}", e.getEventId(), ex);

                e.setRetryCount(e.getRetryCount() + 1);
            }
        }
    }
}