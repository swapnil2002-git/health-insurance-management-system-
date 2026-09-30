package com.healthinsurance.claims.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimEvent {

    private String eventId;
    private String eventType;
    @Builder.Default
    private Integer eventVersion = 1;
    private Instant timestamp;
    private String correlationId;
    private String aggregateId;

    private UUID claimId;
    private String claimNumber;
    private UUID policyId;
    private UUID memberId;
    private UUID providerId;
    private String status;
    private BigDecimal totalClaimAmount;
    private BigDecimal approvedAmount;
    private LocalDate serviceDate;
    private String reason;
}

