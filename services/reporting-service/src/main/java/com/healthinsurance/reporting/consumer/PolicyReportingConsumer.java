package com.healthinsurance.reporting.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthinsurance.reporting.event.PolicyIssuedEvent;
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
public class PolicyReportingConsumer {

    private static final Logger log = LoggerFactory.getLogger(PolicyReportingConsumer.class);

    private final ReportingService reportingService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "policy-events", groupId = "${spring.kafka.consumer.group-id:reporting-service-v3}")
    public void consumePolicyEvent(org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record) {
        try {
            String payload = record.value();
            log.info("Reporting service received event on 'policy-events': key={}, payload={}", record.key(), payload);
            if (payload == null || payload.isBlank()) {
                return;
            }

            PolicyIssuedEvent event;
            String cleanMessage = payload.trim();
            if (cleanMessage.startsWith("\"") && cleanMessage.endsWith("\"")) {
                cleanMessage = objectMapper.readValue(cleanMessage, String.class);
            }
            event = objectMapper.readValue(cleanMessage, PolicyIssuedEvent.class);

            if (event == null || event.getPolicyId() == null) {
                log.warn("Discarding invalid policy event in reporting consumer");
                return;
            }

            String eventId = "REPORT_POLICY_" + event.getPolicyId();
            LocalDate eventDate = (event.getEffectiveDate() != null)
                    ? event.getEffectiveDate().atZone(ZoneId.systemDefault()).toLocalDate()
                    : LocalDate.now();

            reportingService.recordPolicyIssued(eventId, eventDate, event.getStatus());

        } catch (Exception e) {
            log.error("Error processing policy event in reporting service: ", e);
        }
    }
}
