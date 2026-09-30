package com.healthinsurance.quotation.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuoteEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "quote-events";

    public void publishQuoteAcceptedEvent(QuoteGeneratedEvent event) {
        log.info("Publishing QuoteGeneratedEvent to Kafka topic '{}' for Quote ID: {}", TOPIC, event.getQuoteId());
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                kafkaTemplate.send(TOPIC, event.getQuoteId().toString(), event);
                log.info("Successfully sent QuoteGeneratedEvent to Kafka topic '{}' for Quote ID: {}", TOPIC, event.getQuoteId());
            } catch (Exception ex) {
                log.error("Failed to publish QuoteGeneratedEvent to Kafka topic '{}' for Quote ID: {}: {}", TOPIC, event.getQuoteId(), ex.getMessage());
            }
        });
    }
}