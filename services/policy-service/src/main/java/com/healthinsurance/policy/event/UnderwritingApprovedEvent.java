package com.healthinsurance.policy.event;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class UnderwritingApprovedEvent {
    private String eventType;
    private UUID caseId;
    private UUID quoteId;
    private UUID customerId;
    private String decisionType;
    private Instant timestamp;
}
