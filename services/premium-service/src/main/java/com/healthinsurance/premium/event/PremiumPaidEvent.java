package com.healthinsurance.premium.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PremiumPaidEvent {
    private String eventType;
    private UUID paymentId;
    private UUID policyId;
    private UUID installmentId;
    private BigDecimal amount;
    private String paymentStatus;
    private Instant paymentDate;
    private String gatewayReference;
    private Instant timestamp;
}