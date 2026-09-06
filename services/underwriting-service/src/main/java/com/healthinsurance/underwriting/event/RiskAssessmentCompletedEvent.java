package com.healthinsurance.underwriting.event;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class RiskAssessmentCompletedEvent {
    private String eventType;
    private UUID assessmentId;
    private UUID quoteId;
    private UUID customerId;
    private Integer riskScore;
    private String riskClassification;
    private Instant timestamp;
}