package com.healthinsurance.premium.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PremiumEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String SAGA_TOPIC = "policy-events";

    public void publishPremiumScheduleFailed(PremiumScheduleFailedEvent event) {
        log.warn("SAGA COMPENSATION: Publishing PremiumScheduleFailedEvent for Policy ID: {}, Reason: {}",
                event.getPolicyId(), event.getFailureReason());
        kafkaTemplate.send(SAGA_TOPIC, event.getPolicyId().toString(), event);
    }
}
