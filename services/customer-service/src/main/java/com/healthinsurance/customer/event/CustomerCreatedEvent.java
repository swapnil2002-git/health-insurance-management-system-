package com.healthinsurance.customer.event;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class CustomerCreatedEvent {
    private String eventId;
    private String eventType;
    private UUID customerId;
    private Instant timestamp;
    private Long version;
}