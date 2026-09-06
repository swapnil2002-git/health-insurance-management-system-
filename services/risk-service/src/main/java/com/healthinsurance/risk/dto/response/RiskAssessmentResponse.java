package com.healthinsurance.risk.dto.response;
import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class RiskAssessmentResponse {
    private UUID assessmentId;
    private UUID customerId;
    private UUID quoteId;
    private String status;
    private String classification;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;
    
    private List<RiskFactorResponse> factors;
    private List<RiskScoreResponse> scores;
}