package com.healthinsurance.claims.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.healthinsurance.claims.domain.ClaimStatus;
import com.healthinsurance.claims.entity.Claim;
import com.healthinsurance.claims.outbox.OutboxEvent;
import com.healthinsurance.claims.outbox.OutboxEventRepository;
import com.healthinsurance.claims.outbox.OutboxStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @InjectMocks
    private ClaimEventProducer claimEventProducer;

    private Claim claim;
    private UUID claimId;
    private UUID policyId;
    private UUID memberId;

    @BeforeEach
    void setUp() {
        claimId = UUID.randomUUID();
        policyId = UUID.randomUUID();
        memberId = UUID.randomUUID();

        claim = new Claim();
        claim.setClaimId(claimId);
        claim.setClaimNumber("CLM-2026-999");
        claim.setPolicyId(policyId);
        claim.setMemberId(memberId);
        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setTotalClaimAmount(new BigDecimal("1200.00"));
        claim.setServiceDate(LocalDate.now());
    }

    @Test
    void publishClaimSubmitted_PublishesDirectlyToKafka() {
        claimEventProducer.publishClaimSubmitted(claim);

        ArgumentCaptor<ClaimEvent> captor = ArgumentCaptor.forClass(ClaimEvent.class);
        verify(kafkaTemplate, times(1)).send(eq("claim-events"), eq(claimId.toString()), captor.capture());
        verifyNoInteractions(outboxEventRepository);

        ClaimEvent published = captor.getValue();
        assertNotNull(published.getEventId());
        assertEquals("CLAIM_SUBMITTED", published.getEventType());
        assertEquals(1, published.getEventVersion());
        assertNotNull(published.getCorrelationId());
        assertEquals(claimId.toString(), published.getAggregateId());
        assertEquals(claimId, published.getClaimId());
        assertEquals("CLM-2026-999", published.getClaimNumber());
        assertEquals(policyId, published.getPolicyId());
        assertEquals(memberId, published.getMemberId());
        assertEquals("SUBMITTED", published.getStatus());
        assertEquals(new BigDecimal("1200.00"), published.getTotalClaimAmount());
        assertNotNull(published.getTimestamp());
    }

    @Test
    void publishClaimApproved_SavesToTransactionalOutbox() {
        claim.setStatus(ClaimStatus.APPROVED);
        claim.setApprovedAmount(new BigDecimal("1000.00"));

        claimEventProducer.publishClaimApproved(claim);

        verifyNoInteractions(kafkaTemplate);

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository, times(1)).save(captor.capture());

        OutboxEvent outboxEvent = captor.getValue();
        assertNotNull(outboxEvent.getEventId());
        assertEquals("CLAIM_APPROVED", outboxEvent.getEventType());
        assertEquals(claimId.toString(), outboxEvent.getAggregateId());
        assertEquals("claim-events", outboxEvent.getTopic());
        assertEquals(claimId.toString(), outboxEvent.getPartitionKey());
        assertEquals(OutboxStatus.PENDING, outboxEvent.getStatus());
        assertNotNull(outboxEvent.getPayload());
        assertTrue(outboxEvent.getPayload().contains("CLAIM_APPROVED"));
        assertTrue(outboxEvent.getPayload().contains("1000.00"));
        assertNotNull(outboxEvent.getCreatedAt());
    }

    @Test
    void publishClaimSettled_SavesToTransactionalOutbox() {
        claim.setStatus(ClaimStatus.SETTLED);
        claim.setApprovedAmount(new BigDecimal("1000.00"));

        claimEventProducer.publishClaimSettled(claim);

        verifyNoInteractions(kafkaTemplate);

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository, times(1)).save(captor.capture());

        OutboxEvent outboxEvent = captor.getValue();
        assertNotNull(outboxEvent.getEventId());
        assertEquals("CLAIM_SETTLED", outboxEvent.getEventType());
        assertEquals(claimId.toString(), outboxEvent.getAggregateId());
        assertEquals("claim-events", outboxEvent.getTopic());
        assertEquals(claimId.toString(), outboxEvent.getPartitionKey());
        assertEquals(OutboxStatus.PENDING, outboxEvent.getStatus());
        assertNotNull(outboxEvent.getPayload());
        assertTrue(outboxEvent.getPayload().contains("CLAIM_SETTLED"));
    }
}

