package com.healthinsurance.risk.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RiskAssessmentCompletedEvent {
    private String eventType = "RISK_ASSESSMENT_COMPLETED";
    private UUID assessmentId;
    private UUID quoteId;
    private UUID customerId;
    private Integer riskScore;
    private String riskClassification;
    private Instant timestamp;
}