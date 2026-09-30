package com.healthinsurance.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthinsurance.notification.domain.NotificationChannel;
import com.healthinsurance.notification.dto.NotificationRequest;
import com.healthinsurance.notification.event.PremiumPaidEvent;
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
public class PremiumEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PremiumEventConsumer.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "premium-events", groupId = "${spring.kafka.consumer.group-id:notification-service-group}")
    public void consumePremiumEvent(Object payload) {
        try {
            log.info("Received raw event on 'premium-events': {}", payload);
            PremiumPaidEvent event;
            if (payload instanceof PremiumPaidEvent) {
                event = (PremiumPaidEvent) payload;
            } else if (payload instanceof String) {
                String cleanMessage = (String) payload;
                if (cleanMessage.startsWith("\"") && cleanMessage.endsWith("\"")) {
                    cleanMessage = objectMapper.readValue(cleanMessage, String.class);
                }
                event = objectMapper.readValue(cleanMessage, PremiumPaidEvent.class);
            } else {
                event = objectMapper.convertValue(payload, PremiumPaidEvent.class);
            }

            if (event == null || event.getPaymentId() == null) {
                log.warn("Discarding invalid or unparseable premium event");
                return;
            }

            log.info("Processing PremiumPaidEvent for paymentId: {}, amount: {}", event.getPaymentId(), event.getAmount());

            Map<String, Object> data = new HashMap<>();
            data.put("paymentId", event.getPaymentId().toString());
            data.put("policyId", event.getPolicyId() != null ? event.getPolicyId().toString() : "");
            data.put("amount", event.getAmount() != null ? event.getAmount().toString() : "0.00");
            data.put("gatewayReference", event.getGatewayReference() != null ? event.getGatewayReference() : "N/A");
            data.put("paymentDate", event.getPaymentDate() != null ? event.getPaymentDate().toString() : "");

            String recipient = "policy-holder-" + (event.getPolicyId() != null ? event.getPolicyId() : "general") + "@hims.com";

            // Send EMAIL notification
            NotificationRequest emailReq = NotificationRequest.builder()
                    .recipient(recipient)
                    .channel(NotificationChannel.EMAIL)
                    .eventType("PREMIUM_PAID")
                    .referenceId(event.getPaymentId())
                    .eventId("PREMIUM_PAID_" + event.getPaymentId() + "_EMAIL")
                    .templateData(data)
                    .build();
            notificationService.sendNotification(emailReq);

            // Send SMS notification
            NotificationRequest smsReq = NotificationRequest.builder()
                    .recipient("+1-555-0199")
                    .channel(NotificationChannel.SMS)
                    .eventType("PREMIUM_PAID")
                    .referenceId(event.getPaymentId())
                    .eventId("PREMIUM_PAID_" + event.getPaymentId() + "_SMS")
                    .templateData(data)
                    .build();
            notificationService.sendNotification(smsReq);

        } catch (Exception e) {
            log.error("Failed to process premium event: ", e);
        }
    }
}
