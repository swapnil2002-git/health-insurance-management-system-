package com.healthinsurance.payment.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PremiumPaidEvent {
    private String eventType = "PREMIUM_PAID";
    private UUID paymentId;
    private UUID policyId;
    private UUID installmentId;
    private BigDecimal amount;
    private String paymentStatus = "SUCCESS";
    private Instant paymentDate;
    private String gatewayReference;
    private Instant timestamp = Instant.now();
}