package com.healthinsurance.risk.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RiskEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "risk-events";

    public void publishRiskCompletedEvent(RiskAssessmentCompletedEvent event) {
        log.info("Publishing RiskAssessmentCompletedEvent to Kafka topic '{}' for Assessment ID: {}", TOPIC, event.getAssessmentId());
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                kafkaTemplate.send(TOPIC, event.getAssessmentId().toString(), event);
                log.info("Successfully published RiskAssessmentCompletedEvent to Kafka topic '{}' for Assessment ID: {}", TOPIC, event.getAssessmentId());
            } catch (Exception ex) {
                log.error("Failed to publish RiskAssessmentCompletedEvent to Kafka topic '{}' for Assessment ID: {}: {}", TOPIC, event.getAssessmentId(), ex.getMessage());
            }
        });
    }
}