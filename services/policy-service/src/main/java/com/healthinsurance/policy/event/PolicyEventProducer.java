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
        kafkaTemplate.send(TOPIC, event.getPolicyId().toString(), event);
    }
}
