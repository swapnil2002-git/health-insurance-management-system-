package com.healthinsurance.policy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthinsurance.policy.dto.response.PolicyResponse;
import com.healthinsurance.policy.entity.Policy;
import com.healthinsurance.policy.enums.PolicyStatus;
import com.healthinsurance.policy.event.PolicyEventProducer;
import com.healthinsurance.policy.mapper.PolicyMapper;
import com.healthinsurance.policy.outbox.OutboxEvent;
import com.healthinsurance.policy.outbox.OutboxEventRepository;
import com.healthinsurance.policy.outbox.OutboxStatus;
import com.healthinsurance.policy.repository.PolicyRepository;
import com.healthinsurance.policy.service.impl.PolicyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class PolicyIssueServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PolicyMapper mapper;

    @Mock
    private PolicyEventProducer eventProducer;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private com.healthinsurance.policy.audit.PolicyAuditTrailService auditTrailService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @InjectMocks
    private PolicyServiceImpl policyService;

    private Policy testPolicy;
    private UUID policyId;
    private UUID customerId;
    private UUID quoteId;
    private UUID planId;

    @BeforeEach
    void setUp() {
        policyId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        quoteId = UUID.randomUUID();
        planId = UUID.randomUUID();

        testPolicy = new Policy();
        testPolicy.setPolicyId(policyId);
        testPolicy.setCustomerId(customerId);
        testPolicy.setQuoteId(quoteId);
        testPolicy.setPlanId(planId);
        testPolicy.setStatus(PolicyStatus.DRAFT);
        testPolicy.setEffectiveDate(Instant.now());
        testPolicy.setExpiryDate(Instant.now());
    }

    @Test
    void issuePolicy_AtomicallySavesOutboxEventInsideTransaction() {
        when(policyRepository.findById(policyId)).thenReturn(Optional.of(testPolicy));
        when(policyRepository.save(any(Policy.class))).thenAnswer(i -> i.getArgument(0));

        PolicyResponse mockResponse = new PolicyResponse();
        mockResponse.setPolicyId(policyId);
        when(mapper.toResponse(any(Policy.class))).thenReturn(mockResponse);

        PolicyResponse response = policyService.issuePolicy(policyId);

        assertNotNull(response);
        assertEquals(policyId, response.getPolicyId());

        // Verify that Outbox record is saved atomically
        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository, times(1)).save(captor.capture());

        OutboxEvent outbox = captor.getValue();
        assertNotNull(outbox.getEventId());
        assertEquals("PolicyIssued", outbox.getEventType());
        assertEquals(policyId.toString(), outbox.getAggregateId());
        assertEquals("policy-events", outbox.getTopic());
        assertEquals(policyId.toString(), outbox.getPartitionKey());
        assertEquals(OutboxStatus.PENDING, outbox.getStatus());
        assertTrue(outbox.getPayload().contains("PolicyIssued"));
        assertTrue(outbox.getPayload().contains(policyId.toString()));
        assertNotNull(outbox.getCreatedAt());

        // Verify direct publisher is NOT called during issue transaction
        verify(eventProducer, never()).publishPolicyIssued(any());
    }
}
