package com.healthinsurance.reporting.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthinsurance.reporting.event.PremiumPaidEvent;
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
public class PremiumReportingConsumer {

    private static final Logger log = LoggerFactory.getLogger(PremiumReportingConsumer.class);

    private final ReportingService reportingService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "premium-events", groupId = "${spring.kafka.consumer.group-id:reporting-service-v3}")
    public void consumePremiumEvent(org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record) {
        try {
            String payload = record.value();
            log.info("Reporting service received event on 'premium-events': key={}, payload={}", record.key(), payload);
            if (payload == null || payload.isBlank()) {
                return;
            }

            PremiumPaidEvent event;
            String cleanMessage = payload.trim();
            if (cleanMessage.startsWith("\"") && cleanMessage.endsWith("\"")) {
                cleanMessage = objectMapper.readValue(cleanMessage, String.class);
            }
            event = objectMapper.readValue(cleanMessage, PremiumPaidEvent.class);

            if (event == null || event.getPaymentId() == null) {
                log.warn("Discarding invalid premium event in reporting consumer");
                return;
            }

            String eventId = "REPORT_PREMIUM_" + event.getPaymentId();
            LocalDate eventDate = (event.getPaymentDate() != null)
                    ? event.getPaymentDate().atZone(ZoneId.systemDefault()).toLocalDate()
                    : LocalDate.now();

            reportingService.recordPremiumPayment(eventId, eventDate, event.getAmount());

        } catch (Exception e) {
            log.error("Error processing premium event in reporting service: ", e);
        }
    }
}
