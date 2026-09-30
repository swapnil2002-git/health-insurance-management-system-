package com.healthinsurance.policy.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.healthinsurance.policy.entity.Policy;
import com.healthinsurance.policy.enums.PolicyStatus;
import com.healthinsurance.policy.repository.PolicyRepository;
import com.healthinsurance.policy.service.impl.PolicyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicySagaCompensationTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private com.healthinsurance.policy.mapper.PolicyMapper mapper;

    @InjectMocks
    private PolicyServiceImpl policyService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private PolicySagaCompensationConsumer compensationConsumer;

    private UUID policyId;
    private Policy policy;

    @BeforeEach
    void setUp() {
        policyId = UUID.randomUUID();
        policy = new Policy();
        policy.setPolicyId(policyId);
        policy.setPolicyNumber("POL-2026-TEST1234");
        policy.setStatus(PolicyStatus.PENDING_PAYMENT);
        policy.setCreatedAt(Instant.now());
        policy.setUpdatedAt(Instant.now());

        compensationConsumer = new PolicySagaCompensationConsumer(policyService, objectMapper);
    }

    @Test
    void testCompensation_WhenDownstreamScheduleFails_TransitionsPolicyToSuspended() throws Exception {
        when(policyRepository.findById(policyId)).thenReturn(Optional.of(policy));
        when(policyRepository.save(any(Policy.class))).thenAnswer(i -> i.getArgument(0));

        PremiumScheduleFailedEvent event = PremiumScheduleFailedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("PremiumScheduleFailed")
                .eventVersion(1)
                .timestamp(Instant.now())
                .correlationId(UUID.randomUUID().toString())
                .aggregateId(policyId.toString())
                .policyId(policyId)
                .policyNumber("POL-2026-TEST1234")
                .failureReason("Quotation calculation mismatch")
                .build();

        String eventJson = objectMapper.writeValueAsString(event);

        // Act: Consumer receives failure event
        compensationConsumer.consumeCompensationEvent(eventJson);

        // Assert: Local compensating transaction committed policy as SUSPENDED
        assertEquals(PolicyStatus.SUSPENDED, policy.getStatus());
        verify(policyRepository, times(1)).save(policy);
    }

    @Test
    void testCompensation_Idempotent_WhenPolicyAlreadyActive_DoesNotSuspend() throws Exception {
        policy.setStatus(PolicyStatus.ACTIVE);
        when(policyRepository.findById(policyId)).thenReturn(Optional.of(policy));

        PremiumScheduleFailedEvent event = PremiumScheduleFailedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("PremiumScheduleFailed")
                .eventVersion(1)
                .timestamp(Instant.now())
                .correlationId(UUID.randomUUID().toString())
                .aggregateId(policyId.toString())
                .policyId(policyId)
                .policyNumber("POL-2026-TEST1234")
                .failureReason("Late delayed failure message")
                .build();

        String eventJson = objectMapper.writeValueAsString(event);

        // Act
        compensationConsumer.consumeCompensationEvent(eventJson);

        // Assert: Policy remains ACTIVE, not corrupted by late compensation
        assertEquals(PolicyStatus.ACTIVE, policy.getStatus());
        verify(policyRepository, never()).save(policy);
    }

    @Test
    void testCompensation_IgnoresUnrelatedEvents() {
        String otherEventJson = "{\"eventType\":\"PolicyIssued\",\"policyId\":\"" + policyId + "\"}";
        
        compensationConsumer.consumeCompensationEvent(otherEventJson);

        verify(policyRepository, never()).findById(any());
    }
}
