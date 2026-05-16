package com.system.orders.kafka;

import org.springframework.messaging.support.MessageBuilder;
import com.system.orders.entity.OutboxEvent;
import com.system.orders.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

        List<OutboxEvent> events = repo.findTop50ByProcessedFalseOrderByCreatedAtAsc();

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