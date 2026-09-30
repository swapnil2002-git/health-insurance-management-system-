package com.healthinsurance.reporting.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthinsurance.reporting.event.ClaimEvent;
import com.healthinsurance.reporting.service.ReportingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class ClaimReportingConsumer {

    private static final Logger log = LoggerFactory.getLogger(ClaimReportingConsumer.class);

    private final ReportingService reportingService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "claim-events", groupId = "${spring.kafka.consumer.group-id:reporting-service-v3}")
    public void consumeClaimEvent(org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record) {
        try {
            String payload = record.value();
            log.info("Reporting service received event on 'claim-events': key={}, payload={}", record.key(), payload);
            if (payload == null || payload.isBlank()) {
                return;
            }

            ClaimEvent event;
            String cleanMessage = payload.trim();
            if (cleanMessage.startsWith("\"") && cleanMessage.endsWith("\"")) {
                cleanMessage = objectMapper.readValue(cleanMessage, String.class);
            }
            event = objectMapper.readValue(cleanMessage, ClaimEvent.class);

            if (event == null || event.getClaimId() == null) {
                log.warn("Discarding invalid claim event in reporting consumer");
                return;
            }

            String eventType = (event.getEventType() != null) ? event.getEventType() : "CLAIM_EVENT";
            String eventId = "REPORT_CLAIM_" + event.getClaimId() + "_" + eventType;
            LocalDate eventDate = (event.getTimestamp() != null)
                    ? event.getTimestamp().atZone(ZoneId.systemDefault()).toLocalDate()
                    : (event.getServiceDate() != null ? event.getServiceDate() : LocalDate.now());

            reportingService.recordClaimEvent(
                    eventId,
                    eventDate,
                    eventType,
                    event.getStatus(),
                    event.getTotalClaimAmount(),
                    event.getApprovedAmount()
            );

        } catch (Exception e) {
            log.error("Error processing claim event in reporting service: ", e);
        }
    }
}
