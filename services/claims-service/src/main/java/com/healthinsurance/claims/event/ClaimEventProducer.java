package com.healthinsurance.claims.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthinsurance.claims.entity.Claim;
import com.healthinsurance.claims.outbox.OutboxEvent;
import com.healthinsurance.claims.outbox.OutboxEventRepository;
import com.healthinsurance.claims.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    public static final String TOPIC = "claim-events";

    public void publishClaimSubmitted(Claim claim) {
        publishEvent("CLAIM_SUBMITTED", claim, null, false);
    }

    public void publishClaimValidated(Claim claim) {
        publishEvent("CLAIM_VALIDATED", claim, null, false);
    }

    public void publishClaimApproved(Claim claim) {
        // Critical event: Transactional Outbox
        publishEvent("CLAIM_APPROVED", claim, null, true);
    }

    public void publishClaimRejected(Claim claim, String reason) {
        publishEvent("CLAIM_REJECTED", claim, reason, false);
    }

    public void publishClaimSettled(Claim claim) {
        // Critical event: Transactional Outbox
        publishEvent("CLAIM_SETTLED", claim, null, true);
    }

    private void publishEvent(String eventType, Claim claim, String reason, boolean useOutbox) {
        String eventId = UUID.randomUUID().toString();
        ClaimEvent event = ClaimEvent.builder()
                .eventId(eventId)
                .eventType(eventType)
                .eventVersion(1)
                .timestamp(Instant.now())
                .correlationId(UUID.randomUUID().toString())
                .aggregateId(claim.getClaimId() != null ? claim.getClaimId().toString() : null)
                .claimId(claim.getClaimId())
                .claimNumber(claim.getClaimNumber())
                .policyId(claim.getPolicyId())
                .memberId(claim.getMemberId())
                .providerId(claim.getProviderId())
                .status(claim.getStatus() != null ? claim.getStatus().name() : null)
                .totalClaimAmount(claim.getTotalClaimAmount())
                .approvedAmount(claim.getApprovedAmount())
                .serviceDate(claim.getServiceDate())
                .reason(reason)
                .build();

        if (useOutbox) {
            try {
                String payloadJson = objectMapper.writeValueAsString(event);
                OutboxEvent outboxEvent = OutboxEvent.builder()
                        .eventId(event.getEventId())
                        .eventType(event.getEventType())
                        .aggregateId(event.getAggregateId())
                        .correlationId(event.getCorrelationId())
                        .topic(TOPIC)
                        .partitionKey(claim.getClaimId().toString())
                        .payload(payloadJson)
                        .status(OutboxStatus.PENDING)
                        .createdAt(Instant.now())
                        .build();
                outboxEventRepository.save(outboxEvent);
                log.info("Critical claim event {} saved to Transactional Outbox with eventId: {}", eventType, eventId);
            } catch (Exception ex) {
                log.error("Failed to save critical {} event to outbox for claim {}: {}", eventType, claim.getClaimNumber(), ex.getMessage());
                throw new RuntimeException("Failed to save transactional outbox event for claim", ex);
            }
        } else {
            log.info("Publishing non-critical {} event directly to Kafka topic '{}' for Claim Number: {}", eventType, TOPIC, claim.getClaimNumber());
            try {
                kafkaTemplate.send(TOPIC, claim.getClaimId().toString(), event);
            } catch (Exception ex) {
                log.error("Failed to publish {} event to Kafka topic '{}': {}", eventType, TOPIC, ex.getMessage());
            }
        }
    }
}

