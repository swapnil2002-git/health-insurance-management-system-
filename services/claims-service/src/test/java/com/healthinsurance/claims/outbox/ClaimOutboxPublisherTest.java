package com.healthinsurance.claims.outbox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimOutboxPublisherTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private ClaimOutboxPublisher outboxPublisher;

    private OutboxEvent pendingEvent;

    @BeforeEach
    void setUp() {
        pendingEvent = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .eventId("EVT-300")
                .eventType("CLAIM_APPROVED")
                .aggregateId("CLM-300")
                .topic("claim-events")
                .partitionKey("CLM-300")
                .payload("{\"eventType\":\"CLAIM_APPROVED\"}")
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .maxRetries(5)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void publishPendingEvents_SuccessfulPublish_MarksEventPublished() {
        when(outboxEventRepository.findTop50ByStatusInAndRetryCountLessThanOrderByCreatedAtAsc(any(), eq(5)))
                .thenReturn(List.of(pendingEvent));
        when(kafkaTemplate.send("claim-events", "CLM-300", pendingEvent.getPayload()))
                .thenReturn(CompletableFuture.completedFuture(null));

        outboxPublisher.publishPendingEvents();

        verify(kafkaTemplate, times(1)).send("claim-events", "CLM-300", pendingEvent.getPayload());
        assertEquals(OutboxStatus.PUBLISHED, pendingEvent.getStatus());
        assertNotNull(pendingEvent.getPublishedAt());
        assertNull(pendingEvent.getErrorMessage());
        verify(outboxEventRepository, times(1)).save(pendingEvent);
    }

    @Test
    void publishPendingEvents_KafkaFailure_IncrementsRetryCountAndKeepsPending() {
        when(outboxEventRepository.findTop50ByStatusInAndRetryCountLessThanOrderByCreatedAtAsc(any(), eq(5)))
                .thenReturn(List.of(pendingEvent));
        when(kafkaTemplate.send(any(), any(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Kafka timeout")));

        outboxPublisher.publishPendingEvents();

        verify(kafkaTemplate, times(1)).send("claim-events", "CLM-300", pendingEvent.getPayload());
        assertEquals(1, pendingEvent.getRetryCount());
        assertEquals(OutboxStatus.PENDING, pendingEvent.getStatus());
        assertTrue(pendingEvent.getErrorMessage().contains("Kafka timeout"));
        assertNull(pendingEvent.getPublishedAt());
        verify(outboxEventRepository, times(1)).save(pendingEvent);
    }

    @Test
    void publishPendingEvents_KafkaFailure_ExceedsMaxRetries_MarksAsFailed() {
        pendingEvent.setRetryCount(4);
        pendingEvent.setMaxRetries(5);
        when(outboxEventRepository.findTop50ByStatusInAndRetryCountLessThanOrderByCreatedAtAsc(any(), eq(5)))
                .thenReturn(List.of(pendingEvent));
        when(kafkaTemplate.send(any(), any(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Kafka broker down")));

        outboxPublisher.publishPendingEvents();

        assertEquals(5, pendingEvent.getRetryCount());
        assertEquals(OutboxStatus.FAILED, pendingEvent.getStatus());
        assertTrue(pendingEvent.getErrorMessage().contains("Kafka broker down"));
        verify(outboxEventRepository, times(1)).save(pendingEvent);
    }

    @Test
    void publishPendingEvents_NoPendingEvents_DoesNothing() {
        when(outboxEventRepository.findTop50ByStatusInAndRetryCountLessThanOrderByCreatedAtAsc(any(), eq(5)))
                .thenReturn(Collections.emptyList());

        outboxPublisher.publishPendingEvents();

        verifyNoInteractions(kafkaTemplate);
        verify(outboxEventRepository, never()).save(any());
    }
}
