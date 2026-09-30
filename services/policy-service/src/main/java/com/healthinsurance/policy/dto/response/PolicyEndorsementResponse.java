package com.healthinsurance.policy.dto.response;

import com.healthinsurance.policy.enums.EndorsementStatus;
import com.healthinsurance.policy.enums.EndorsementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyEndorsementResponse {
    private UUID endorsementId;
    private UUID policyId;
    private String policyNumber;
    private EndorsementType endorsementType;
    private EndorsementStatus status;
    private String description;
    private Map<String, Object> changeData;
    private BigDecimal revisedPremium;
    private String requestedBy;
    private String approvedBy;
    private String rejectionReason;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant appliedAt;
}