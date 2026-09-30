package com.healthinsurance.policy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RenewalEligibilityResponse {
    private UUID policyId;
    private String policyNumber;
    private boolean eligible;
    private String reason;
    private Instant currentExpiryDate;
    private long daysUntilExpiry;
}
