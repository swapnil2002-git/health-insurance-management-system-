package com.healthinsurance.customer.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerEventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "customer-events";

    public void publishCustomerCreatedEvent(CustomerCreatedEvent event) {
        log.info("Publishing CustomerCreatedEvent to topic {} for customerId: {}", TOPIC, event.getCustomerId());
        kafkaTemplate.send(TOPIC, event.getCustomerId().toString(), event);
    }
}