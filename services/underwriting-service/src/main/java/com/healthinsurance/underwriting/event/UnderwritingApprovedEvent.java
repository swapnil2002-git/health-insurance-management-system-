package com.healthinsurance.underwriting.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnderwritingApprovedEvent {
    private String eventType = "UNDERWRITING_APPROVED";
    private UUID caseId;
    private UUID quoteId;
    private UUID customerId;
    private String decisionType;
    private Instant timestamp;
}