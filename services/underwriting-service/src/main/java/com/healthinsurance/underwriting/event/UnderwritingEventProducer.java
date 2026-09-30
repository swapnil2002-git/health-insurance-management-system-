package com.healthinsurance.underwriting.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnderwritingEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "underwriting-events";

    public void publishApprovalEvent(UnderwritingApprovedEvent event) {
        log.info("Publishing UnderwritingApprovedEvent to Kafka topic '{}' for Quote ID: {}", TOPIC, event.getQuoteId());
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                kafkaTemplate.send(TOPIC, event.getQuoteId().toString(), event);
                log.info("Successfully published UnderwritingApprovedEvent to Kafka topic '{}' for Quote ID: {}", TOPIC, event.getQuoteId());
            } catch (Exception ex) {
                log.error("Failed to publish UnderwritingApprovedEvent to Kafka topic '{}' for Quote ID: {}: {}", TOPIC, event.getQuoteId(), ex.getMessage());
            }
        });
    }
}