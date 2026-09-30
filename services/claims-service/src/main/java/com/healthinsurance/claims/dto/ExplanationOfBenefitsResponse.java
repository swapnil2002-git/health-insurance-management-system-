package com.healthinsurance.claims.dto;

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
public class ExplanationOfBenefitsResponse {

    private UUID eobId;
    private UUID claimId;
    private String eobNumber;
    private BigDecimal claimAmount;
    private BigDecimal allowedAmount;
    private BigDecimal deductible;
    private BigDecimal copay;
    private BigDecimal insurancePayment;
    private BigDecimal customerResponsibility;
    private String remarks;
    private Instant generatedAt;
}
