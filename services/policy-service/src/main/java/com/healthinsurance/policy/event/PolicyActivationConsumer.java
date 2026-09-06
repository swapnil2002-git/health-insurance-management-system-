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
public class PolicyActivationConsumer {

    private final PolicyService policyService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "premium-events", groupId = "policy-payment-group")
    public void consumePremiumPaid(String message) {
        log.info("=========================================================");
        log.info("POLICY SERVICE KAFKA LISTENER: Received raw message on 'premium-events': {}", message);

        try {
            PremiumPaidEvent event = objectMapper.readValue(message, PremiumPaidEvent.class);
            if (event.getPolicyId() == null) {
                log.warn("Policy ID is null in PremiumPaidEvent. Skipping activation.");
                return;
            }

            log.info("Activating Policy ID: {} upon successful premium payment confirmation...", event.getPolicyId());
            policyService.activatePolicy(event.getPolicyId());
            log.info("SUCCESS: Policy ID {} has been officially transitioned to ACTIVE status!", event.getPolicyId());
        } catch (Exception e) {
            log.error("Failed to process PremiumPaidEvent in policy-service: {}", e.getMessage());
        }
        log.info("=========================================================");
    }
}