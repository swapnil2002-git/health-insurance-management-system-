package com.healthinsurance.reporting.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolicyIssuedEvent {
    private String eventId;
    @Builder.Default
    private String eventType = "PolicyIssued";
    @Builder.Default
    private Integer eventVersion = 1;
    private Instant timestamp;
    private String correlationId;
    private String aggregateId;

    private UUID policyId;
    private String policyNumber;
    private UUID customerId;
    private UUID quoteId;
    private UUID planId;
    private Instant effectiveDate;
    private Instant expiryDate;
    private String status;
}

