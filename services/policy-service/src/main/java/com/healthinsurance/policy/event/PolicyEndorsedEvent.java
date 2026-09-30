package com.healthinsurance.policy.event;

import com.healthinsurance.policy.enums.EndorsementType;
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
public class PolicyEndorsedEvent {
    @Builder.Default
    private String eventType = "POLICY_ENDORSED";
    private UUID endorsementId;
    private UUID policyId;
    private String policyNumber;
    private UUID customerId;
    private EndorsementType endorsementType;
    private String description;
    private BigDecimal revisedPremium;
    private String approvedBy;
    @Builder.Default
    private Instant timestamp = Instant.now();
}
