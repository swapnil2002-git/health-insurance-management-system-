package com.healthinsurance.premium.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyIssuedEvent {
    private String eventType;
    private UUID policyId;
    private String policyNumber;
    private UUID customerId;
    private UUID quoteId;
    private UUID planId;
    private Instant effectiveDate;
    private Instant expiryDate;
    private String status;
    private Instant timestamp;
}