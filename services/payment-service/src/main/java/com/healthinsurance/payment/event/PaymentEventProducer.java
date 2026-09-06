package com.healthinsurance.payment.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "premium-events";

    public void publishPremiumPaid(PremiumPaidEvent event) {
        log.info("Publishing PremiumPaidEvent to Kafka topic '{}' for Payment ID: {}, Policy ID: {}, Amount: {}",
                TOPIC, event.getPaymentId(), event.getPolicyId(), event.getAmount());
        kafkaTemplate.send(TOPIC, event.getPolicyId().toString(), event);
    }
}