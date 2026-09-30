package com.healthinsurance.policy.event;

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
public class PremiumScheduleFailedEvent {
    private String eventId;
    @Builder.Default
    private String eventType = "PremiumScheduleFailed";
    @Builder.Default
    private Integer eventVersion = 1;
    private Instant timestamp;
    private String correlationId;
    private String aggregateId;

    private UUID policyId;
    private String policyNumber;
    private String failureReason;
}
