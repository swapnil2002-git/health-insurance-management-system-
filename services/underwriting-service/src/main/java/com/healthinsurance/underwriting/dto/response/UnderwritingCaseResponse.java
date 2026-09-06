package com.healthinsurance.underwriting.dto.response;
import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class UnderwritingCaseResponse {
    private UUID caseId;
    private UUID quoteId;
    private UUID customerId;
    private UUID assessmentId;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;
    
    private List<UnderwritingDecisionResponse> decisions;
}