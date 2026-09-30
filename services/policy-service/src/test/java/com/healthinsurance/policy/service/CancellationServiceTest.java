package com.healthinsurance.policy.service;

import com.healthinsurance.policy.client.PaymentClient;
import com.healthinsurance.policy.dto.request.CancellationApprovalRequest;
import com.healthinsurance.policy.dto.request.CancellationRejectRequest;
import com.healthinsurance.policy.dto.request.PolicyCancellationRequest;
import com.healthinsurance.policy.dto.response.PolicyCancellationResponse;
import com.healthinsurance.policy.entity.Policy;
import com.healthinsurance.policy.entity.PolicyCancellation;
import com.healthinsurance.policy.enums.CancellationStatus;
import com.healthinsurance.policy.enums.PolicyStatus;
import com.healthinsurance.policy.event.PolicyCancelledEvent;
import com.healthinsurance.policy.event.PolicyEventProducer;
import com.healthinsurance.policy.exception.InvalidPolicyStateException;
import com.healthinsurance.policy.repository.PolicyCancellationRepository;
import com.healthinsurance.policy.repository.PolicyRepository;
import com.healthinsurance.policy.service.impl.CancellationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CancellationServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PolicyCancellationRepository cancellationRepository;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private PolicyEventProducer policyEventProducer;

    @Mock
    private com.healthinsurance.policy.audit.PolicyAuditTrailService auditTrailService;

    private CancellationServiceImpl cancellationService;

    private Policy testPolicy;
    private UUID policyId;

    @BeforeEach
    void setUp() {
        cancellationService = new CancellationServiceImpl(
                policyRepository,
                cancellationRepository,
                paymentClient,
                policyEventProducer,
                auditTrailService
        );

        policyId = UUID.randomUUID();
        testPolicy = new Policy();
        testPolicy.setPolicyId(policyId);
        testPolicy.setPolicyNumber("POL-2026-CANCEL01");
        testPolicy.setCustomerId(UUID.randomUUID());
        testPolicy.setPlanId(UUID.randomUUID());
        testPolicy.setQuoteId(UUID.randomUUID());
        testPolicy.setStatus(PolicyStatus.ACTIVE);
        testPolicy.setEffectiveDate(Instant.now().minus(30, ChronoUnit.DAYS));
        testPolicy.setExpiryDate(Instant.now().plus(335, ChronoUnit.DAYS));
        testPolicy.setMembers(new HashSet<>());
        testPolicy.setCoverages(new HashSet<>());
        testPolicy.setBeneficiaries(new HashSet<>());
    }

    @Test
    void testRequestCancellation_ActivePolicy_Success() {
        PolicyCancellationRequest request = PolicyCancellationRequest.builder()
                .reason("Relocating abroad")
                .requestedBy("POLICY_HOLDER")
                .build();

        when(policyRepository.findById(policyId)).thenReturn(Optional.of(testPolicy));
        when(cancellationRepository.existsByPolicy_PolicyIdAndStatusIn(eq(policyId), anyList())).thenReturn(false);
        when(cancellationRepository.save(any(PolicyCancellation.class))).thenAnswer(invocation -> {
            PolicyCancellation c = invocation.getArgument(0);
            c.setCancellationId(UUID.randomUUID());
            return c;
        });

        PolicyCancellationResponse response = cancellationService.requestCancellation(policyId, request);

        assertNotNull(response);
        assertEquals(CancellationStatus.PENDING_APPROVAL, response.getStatus());
        assertEquals("Relocating abroad", response.getReason());
        assertNotNull(response.getRefundAmount());
        assertTrue(response.getRefundAmount().compareTo(BigDecimal.ZERO) > 0);
        verify(cancellationRepository).save(any(PolicyCancellation.class));
    }

    @Test
    void testRequestCancellation_NonActivePolicy_ThrowsException() {
        testPolicy.setStatus(PolicyStatus.CANCELLED);
        when(policyRepository.findById(policyId)).thenReturn(Optional.of(testPolicy));

        PolicyCancellationRequest request = PolicyCancellationRequest.builder()
                .reason("Already cancelled")
                .build();

        assertThrows(InvalidPolicyStateException.class, () -> cancellationService.requestCancellation(policyId, request));
    }

    @Test
    void testRequestCancellation_AlreadyPending_ThrowsException() {
        when(policyRepository.findById(policyId)).thenReturn(Optional.of(testPolicy));
        when(cancellationRepository.existsByPolicy_PolicyIdAndStatusIn(eq(policyId), anyList())).thenReturn(true);

        PolicyCancellationRequest request = PolicyCancellationRequest.builder()
                .reason("Duplicate request")
                .build();

        assertThrows(InvalidPolicyStateException.class, () -> cancellationService.requestCancellation(policyId, request));
    }

    @Test
    void testApproveCancellation_Success_TriggersRefundAndCancelsPolicy() {
        UUID cancellationId = UUID.randomUUID();
        PolicyCancellation cancellation = PolicyCancellation.builder()
                .cancellationId(cancellationId)
                .policy(testPolicy)
                .status(CancellationStatus.PENDING_APPROVAL)
                .reason("Customer request")
                .refundAmount(new BigDecimal("900.00"))
                .requestedBy("CUSTOMER")
                .build();

        when(cancellationRepository.findByPolicy_PolicyId(policyId)).thenReturn(Optional.of(cancellation));
        when(cancellationRepository.save(any(PolicyCancellation.class))).thenAnswer(i -> i.getArgument(0));
        when(policyRepository.save(any(Policy.class))).thenAnswer(i -> i.getArgument(0));

        UUID paymentId = UUID.randomUUID();
        UUID refundId = UUID.randomUUID();
        when(paymentClient.getPaymentsByPolicy(policyId)).thenReturn(List.of(
                Map.of("paymentId", paymentId.toString(), "status", "SUCCESS")
        ));
        when(paymentClient.processRefund(eq(paymentId), anyMap())).thenReturn(Map.of("refundId", refundId.toString()));

        CancellationApprovalRequest approval = CancellationApprovalRequest.builder()
                .approvedBy("OPERATIONS_MANAGER")
                .remarks("Eligible for refund")
                .build();

        PolicyCancellationResponse response = cancellationService.approveCancellation(policyId, approval);

        assertNotNull(response);
        assertEquals(CancellationStatus.CANCELLED, response.getStatus());
        assertEquals("OPERATIONS_MANAGER", response.getApprovedBy());
        assertEquals(PolicyStatus.CANCELLED, testPolicy.getStatus());
        assertEquals(refundId, response.getRefundTransactionId());

        // Verify Kafka event published
        verify(policyEventProducer).publishPolicyCancelled(any(PolicyCancelledEvent.class));
    }

    @Test
    void testRejectCancellation_Success() {
        UUID cancellationId = UUID.randomUUID();
        PolicyCancellation cancellation = PolicyCancellation.builder()
                .cancellationId(cancellationId)
                .policy(testPolicy)
                .status(CancellationStatus.PENDING_APPROVAL)
                .reason("Misunderstanding")
                .build();

        when(cancellationRepository.findByPolicy_PolicyId(policyId)).thenReturn(Optional.of(cancellation));
        when(cancellationRepository.save(any(PolicyCancellation.class))).thenAnswer(i -> i.getArgument(0));

        CancellationRejectRequest reject = CancellationRejectRequest.builder()
                .rejectionReason("Customer decided to retain policy")
                .rejectedBy("AGENT_BOB")
                .build();

        PolicyCancellationResponse response = cancellationService.rejectCancellation(policyId, reject);

        assertNotNull(response);
        assertEquals(CancellationStatus.REJECTED, response.getStatus());
        assertEquals("Customer decided to retain policy", response.getRejectionReason());
        // Policy status remains ACTIVE
        assertEquals(PolicyStatus.ACTIVE, testPolicy.getStatus());
        verify(policyRepository, never()).save(any());
        verify(policyEventProducer, never()).publishPolicyCancelled(any());
    }
}
