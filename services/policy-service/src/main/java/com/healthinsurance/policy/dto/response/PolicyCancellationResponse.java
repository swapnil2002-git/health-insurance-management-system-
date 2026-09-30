package com.healthinsurance.policy.dto.response;

import com.healthinsurance.policy.enums.CancellationStatus;
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
public class PolicyCancellationResponse {
    private UUID cancellationId;
    private UUID policyId;
    private String policyNumber;
    private CancellationStatus status;
    private String reason;
    private BigDecimal refundAmount;
    private UUID refundTransactionId;
    private String requestedBy;
    private String approvedBy;
    private String rejectionReason;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant cancelledAt;
}