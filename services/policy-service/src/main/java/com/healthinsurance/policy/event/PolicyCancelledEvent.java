package com.healthinsurance.policy.event;

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
public class PolicyCancelledEvent {
    @Builder.Default
    private String eventType = "POLICY_CANCELLED";
    private UUID cancellationId;
    private UUID policyId;
    private String policyNumber;
    private UUID customerId;
    private String reason;
    private BigDecimal refundAmount;
    private UUID refundTransactionId;
    private String approvedBy;
    @Builder.Default
    private Instant timestamp = Instant.now();
}
