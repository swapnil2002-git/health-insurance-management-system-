package com.healthinsurance.underwriting.dto.response;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class UnderwritingDecisionResponse {
    private UUID decisionId;
    private String decisionType;
    private String reason;
    private String notes;
    private Instant decidedAt;
}