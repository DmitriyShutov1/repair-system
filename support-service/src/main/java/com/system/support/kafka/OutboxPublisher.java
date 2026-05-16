package com.system.support.kafka;

import com.system.support.entity.OutboxEvent;
import com.system.support.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.messaging.support.MessageBuilder;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "operation-events";

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publish() {

        List<OutboxEvent> events = outboxRepo.findTop50ByProcessedFalseOrderByCreatedAtAsc();

        for (OutboxEvent event : events) {
            try {
                kafkaTemplate.send(TOPIC, event.getPayload()).get();

                event.setProcessed(true);
                event.setProcessedAt(LocalDateTime.now());

            } catch (Exception e) {
                log.error("Failed to send event {}", event.getEventId(), e);

                event.setRetryCount(event.getRetryCount() + 1);
            }
        }
    }
}