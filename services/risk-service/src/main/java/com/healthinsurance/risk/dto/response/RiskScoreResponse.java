package com.healthinsurance.risk.dto.response;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class RiskScoreResponse {
    private UUID riskScoreId;
    private Integer score;
    private String classification;
    private Instant calculatedAt;
}