package com.healthinsurance.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthinsurance.notification.domain.NotificationChannel;
import com.healthinsurance.notification.dto.NotificationRequest;
import com.healthinsurance.notification.event.ClaimEvent;
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
public class ClaimEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ClaimEventConsumer.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "claim-events", groupId = "${spring.kafka.consumer.group-id:notification-service-group}")
    public void consumeClaimEvent(Object payload) {
        try {
            log.info("Received raw event on 'claim-events': {}", payload);
            ClaimEvent event;
            if (payload instanceof ClaimEvent) {
                event = (ClaimEvent) payload;
            } else if (payload instanceof String) {
                String cleanMessage = (String) payload;
                if (cleanMessage.startsWith("\"") && cleanMessage.endsWith("\"")) {
                    cleanMessage = objectMapper.readValue(cleanMessage, String.class);
                }
                event = objectMapper.readValue(cleanMessage, ClaimEvent.class);
            } else {
                event = objectMapper.convertValue(payload, ClaimEvent.class);
            }

            if (event == null || event.getClaimId() == null) {
                log.warn("Discarding invalid or unparseable claim event");
                return;
            }

            log.info("Processing ClaimEvent: {} for claimNumber: {}", event.getEventType(), event.getClaimNumber());

            Map<String, Object> data = new HashMap<>();
            data.put("claimId", event.getClaimId().toString());
            data.put("claimNumber", event.getClaimNumber());
            data.put("status", event.getStatus());
            data.put("totalClaimAmount", event.getTotalClaimAmount() != null ? event.getTotalClaimAmount().toString() : "0.00");
            data.put("approvedAmount", event.getApprovedAmount() != null ? event.getApprovedAmount().toString() : "0.00");
            data.put("reason", event.getReason() != null ? event.getReason() : "");

            String recipient = "member-" + (event.getMemberId() != null ? event.getMemberId() : "general") + "@hims.com";
            String eventType = event.getEventType() != null ? event.getEventType() : "CLAIM_UPDATE";

            // Send EMAIL notification
            NotificationRequest emailReq = NotificationRequest.builder()
                    .recipient(recipient)
                    .channel(NotificationChannel.EMAIL)
                    .eventType(eventType)
                    .referenceId(event.getClaimId())
                    .eventId(eventType + "_" + event.getClaimId() + "_EMAIL")
                    .templateData(data)
                    .build();
            notificationService.sendNotification(emailReq);

            // Send SMS notification
            NotificationRequest smsReq = NotificationRequest.builder()
                    .recipient("+1-555-0188")
                    .channel(NotificationChannel.SMS)
                    .eventType(eventType)
                    .referenceId(event.getClaimId())
                    .eventId(eventType + "_" + event.getClaimId() + "_SMS")
                    .templateData(data)
                    .build();
            notificationService.sendNotification(smsReq);

        } catch (Exception e) {
            log.error("Failed to process claim event: ", e);
        }
    }
}
