package com.healthinsurance.notification.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class PremiumPaidEvent {
    private String eventId;
    @Builder.Default
    private String eventType = "PremiumPaid";
    @Builder.Default
    private Integer eventVersion = 1;
    private Instant timestamp;
    private String correlationId;
    private String aggregateId;

    private UUID paymentId;
    private UUID policyId;
    private UUID installmentId;
    private BigDecimal amount;
    @Builder.Default
    private String paymentStatus = "SUCCESS";
    private Instant paymentDate;
    private String gatewayReference;
}

