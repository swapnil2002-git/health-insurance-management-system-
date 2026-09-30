package com.healthinsurance.policy.dto.response;

import com.healthinsurance.policy.enums.RenewalStatus;
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
public class PolicyRenewalResponse {
    private UUID renewalId;
    private UUID policyId;
    private String policyNumber;
    private RenewalStatus status;
    private UUID renewalQuoteId;
    private BigDecimal renewalPremium;
    private UUID paymentId;
    private Instant newEffectiveDate;
    private Instant newExpiryDate;
    private String rejectionReason;
    private Instant createdAt;
    private Instant updatedAt;
}
