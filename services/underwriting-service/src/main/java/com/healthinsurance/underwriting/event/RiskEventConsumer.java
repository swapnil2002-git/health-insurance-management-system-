package com.healthinsurance.underwriting.event;

import com.healthinsurance.underwriting.dto.request.CreateUnderwritingCaseRequest;
import com.healthinsurance.underwriting.service.UnderwritingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RiskEventConsumer {

    private final UnderwritingService underwritingService;

    @KafkaListener(topics = "risk-events", groupId = "underwriting-group")
    public void consumeRiskAssessmentEvent(RiskAssessmentCompletedEvent event) {
        log.info("=========================================================");
        log.info("KAFKA LISTENER TRIGGERED: Risk Assessment Completed Event");
        log.info("Assessment ID: {}", event.getAssessmentId());
        log.info("Classification: {}", event.getRiskClassification());
        log.info("=========================================================");

        try {
            CreateUnderwritingCaseRequest request = new CreateUnderwritingCaseRequest();
            request.setAssessmentId(event.getAssessmentId());
            request.setQuoteId(event.getQuoteId());
            request.setCustomerId(event.getCustomerId());

            // Delegate to the service layer, bypassing synchronous Feign checks
            underwritingService.createCaseFromEvent(request);
        } catch (Exception e) {
            log.error("Failed to process RiskAssessmentCompletedEvent for Assessment ID: {}", event.getAssessmentId(), e);
        }
    }
}