package com.healthinsurance.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthinsurance.notification.domain.NotificationChannel;
import com.healthinsurance.notification.dto.NotificationRequest;
import com.healthinsurance.notification.event.PolicyIssuedEvent;
import com.healthinsurance.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PolicyEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PolicyEventConsumer.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "policy-events", groupId = "${spring.kafka.consumer.group-id:notification-service-group}")
    public void consumePolicyEvent(Object payload) {
        try {
            log.info("Received raw event on 'policy-events': {}", payload);
            PolicyIssuedEvent event;
            if (payload instanceof PolicyIssuedEvent) {
                event = (PolicyIssuedEvent) payload;
            } else if (payload instanceof String) {
                String cleanMessage = (String) payload;
                if (cleanMessage.startsWith("\"") && cleanMessage.endsWith("\"")) {
                    cleanMessage = objectMapper.readValue(cleanMessage, String.class);
                }
                event = objectMapper.readValue(cleanMessage, PolicyIssuedEvent.class);
            } else {
                event = objectMapper.convertValue(payload, PolicyIssuedEvent.class);
            }

            if (event == null || event.getPolicyId() == null) {
                log.warn("Discarding invalid or unparseable policy event");
                return;
            }

            log.info("Processing PolicyIssuedEvent for policyNumber: {}", event.getPolicyNumber());

            Map<String, Object> data = new HashMap<>();
            data.put("policyId", event.getPolicyId().toString());
            data.put("policyNumber", event.getPolicyNumber());
            data.put("customerId", event.getCustomerId() != null ? event.getCustomerId().toString() : "");
            data.put("effectiveDate", event.getEffectiveDate() != null ? event.getEffectiveDate().toString() : "");
            data.put("expiryDate", event.getExpiryDate() != null ? event.getExpiryDate().toString() : "");

            String recipient = "customer-" + (event.getCustomerId() != null ? event.getCustomerId() : "general") + "@hims.com";

            // Send EMAIL notification
            NotificationRequest emailReq = NotificationRequest.builder()
                    .recipient(recipient)
                    .channel(NotificationChannel.EMAIL)
                    .eventType("POLICY_ISSUED")
                    .referenceId(event.getPolicyId())
                    .eventId("POLICY_ISSUED_" + event.getPolicyId() + "_EMAIL")
                    .templateData(data)
                    .build();
            notificationService.sendNotification(emailReq);

            // Send IN_APP notification
            NotificationRequest appReq = NotificationRequest.builder()
                    .recipient(event.getCustomerId() != null ? event.getCustomerId().toString() : "customer")
                    .channel(NotificationChannel.IN_APP)
                    .eventType("POLICY_ISSUED")
                    .referenceId(event.getPolicyId())
                    .eventId("POLICY_ISSUED_" + event.getPolicyId() + "_IN_APP")
                    .templateData(data)
                    .build();
            notificationService.sendNotification(appReq);

        } catch (Exception e) {
            log.error("Failed to process policy event: ", e);
        }
    }
}
