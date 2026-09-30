package com.healthinsurance.policy.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PolicyEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "policy-events";

    public void publishPolicyIssued(PolicyIssuedEvent event) {
        log.info("Publishing PolicyIssuedEvent to Kafka topic '{}' for Policy Number: {}", TOPIC, event.getPolicyNumber());
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                kafkaTemplate.send(TOPIC, event.getPolicyId().toString(), event);
                log.info("Successfully published PolicyIssuedEvent for Policy: {}", event.getPolicyNumber());
            } catch (Exception e) {
                log.error("Failed to publish PolicyIssuedEvent for Policy {}: {}", event.getPolicyNumber(), e.getMessage());
            }
        });
    }

    public void publishPolicyRenewed(PolicyRenewedEvent event) {
        log.info("Publishing PolicyRenewedEvent to Kafka topic '{}' for Policy Number: {}", TOPIC, event.getPolicyNumber());
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                kafkaTemplate.send(TOPIC, event.getPolicyId().toString(), event);
                log.info("Successfully published PolicyRenewedEvent for Policy: {}", event.getPolicyNumber());
            } catch (Exception e) {
                log.error("Failed to publish PolicyRenewedEvent for Policy {}: {}", event.getPolicyNumber(), e.getMessage());
            }
        });
    }

    public void publishPolicyCancelled(PolicyCancelledEvent event) {
        log.info("Publishing PolicyCancelledEvent to Kafka topic '{}' for Policy Number: {}", TOPIC, event.getPolicyNumber());
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                kafkaTemplate.send(TOPIC, event.getPolicyId().toString(), event);
                log.info("Successfully published PolicyCancelledEvent for Policy: {}", event.getPolicyNumber());
            } catch (Exception e) {
                log.error("Failed to publish PolicyCancelledEvent for Policy {}: {}", event.getPolicyNumber(), e.getMessage());
            }
        });
    }

    public void publishPolicyEndorsed(PolicyEndorsedEvent event) {
        log.info("Publishing PolicyEndorsedEvent to Kafka topic '{}' for Policy Number: {}", TOPIC, event.getPolicyNumber());
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                kafkaTemplate.send(TOPIC, event.getPolicyId().toString(), event);
                log.info("Successfully published PolicyEndorsedEvent for Policy: {}", event.getPolicyNumber());
            } catch (Exception e) {
                log.error("Failed to publish PolicyEndorsedEvent for Policy {}: {}", event.getPolicyNumber(), e.getMessage());
            }
        });
    }
}
