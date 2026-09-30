package com.healthinsurance.claims.dto;

import com.healthinsurance.claims.domain.AdjudicationDecision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimAdjudicationResponse {

    private UUID adjudicationId;
    private UUID claimId;
    private BigDecimal submittedAmount;
    private BigDecimal allowedAmount;
    private BigDecimal deductibleAmount;
    private BigDecimal copayAmount;
    private BigDecimal copayPercentage;
    private BigDecimal payableAmount;
    private BigDecimal customerResponsibility;
    private AdjudicationDecision decision;
    private String reason;
    private Instant adjudicatedAt;
}
