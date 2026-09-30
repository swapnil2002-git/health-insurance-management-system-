package com.healthinsurance.claims.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimOutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${outbox.publisher.max-retries:5}")
    private int maxRetries = 5;

    @Scheduled(fixedDelayString = "${outbox.publisher.delay:2000}")
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findTop50ByStatusInAndRetryCountLessThanOrderByCreatedAtAsc(
                Arrays.asList(OutboxStatus.PENDING, OutboxStatus.FAILED),
                maxRetries
        );

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Found {} pending/failed claim outbox events to publish", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            publishSingleEvent(event);
        }
    }

    @Transactional
    public void publishSingleEvent(OutboxEvent event) {
        try {
            log.info("Relaying claim outbox event {} of type {} to topic {}", event.getEventId(), event.getEventType(), event.getTopic());
            kafkaTemplate.send(event.getTopic(), event.getPartitionKey(), event.getPayload()).get();

            event.setStatus(OutboxStatus.PUBLISHED);
            event.setPublishedAt(Instant.now());
            event.setErrorMessage(null);
            outboxEventRepository.save(event);
            log.info("Successfully published claim outbox event: {}", event.getEventId());
        } catch (Exception ex) {
            log.error("Failed to publish claim outbox event {}: {}", event.getEventId(), ex.getMessage());
            int newRetryCount = event.getRetryCount() + 1;
            event.setRetryCount(newRetryCount);
            String errorMsg = ex.getMessage();
            if (errorMsg != null && errorMsg.length() > 1000) {
                errorMsg = errorMsg.substring(0, 1000);
            }
            event.setErrorMessage(errorMsg);
            if (newRetryCount >= event.getMaxRetries()) {
                event.setStatus(OutboxStatus.FAILED);
                log.error("Claim outbox event {} exceeded max retries ({}), marking as FAILED", event.getEventId(), event.getMaxRetries());
            } else {
                event.setStatus(OutboxStatus.PENDING);
            }
            outboxEventRepository.save(event);
        }
    }
}
