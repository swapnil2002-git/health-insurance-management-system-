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
public class PolicyRenewedEvent {
    private String eventId;
    @Builder.Default
    private String eventType = "PolicyRenewed";
    @Builder.Default
    private Integer eventVersion = 1;
    private Instant timestamp;
    private String correlationId;
    private String aggregateId;

    private UUID renewalId;
    private UUID policyId;
    private String policyNumber;
    private UUID customerId;
    private UUID renewalQuoteId;
    private BigDecimal renewalPremium;
    private UUID paymentId;
    private Instant newEffectiveDate;
    private Instant newExpiryDate;
}

