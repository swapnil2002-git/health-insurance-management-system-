package com.healthinsurance.policy.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthinsurance.policy.service.PolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PolicySagaCompensationConsumer {

    private final PolicyService policyService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "policy-events", groupId = "policy-saga-group")
    public void consumeCompensationEvent(String message) {
        if (!message.contains("PremiumScheduleFailed")) {
            return;
        }

        log.warn("=========================================================");
        log.warn("SAGA COMPENSATION CONSUMER TRIGGERED: PremiumScheduleFailed message detected");

        try {
            PremiumScheduleFailedEvent event = objectMapper.readValue(message, PremiumScheduleFailedEvent.class);
            if (event.getPolicyId() == null) {
                log.warn("Policy ID is null in PremiumScheduleFailedEvent. Skipping compensation.");
                return;
            }

            log.warn("Executing compensating transaction for Policy ID: {}, Correlation ID: {}, Reason: {}",
                    event.getPolicyId(), event.getCorrelationId(), event.getFailureReason());

            policyService.compensatePolicyIssuance(event.getPolicyId(), event.getFailureReason());

            log.warn("SAGA COMPENSATION COMPLETED: Policy ID {} successfully compensated.", event.getPolicyId());
        } catch (Exception e) {
            log.error("Failed to execute SAGA compensation for message: {}", message, e);
        }
        log.warn("=========================================================");
    }
}
