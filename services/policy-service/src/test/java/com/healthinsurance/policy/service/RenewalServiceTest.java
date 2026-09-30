package com.healthinsurance.policy.service;

import com.healthinsurance.policy.dto.request.PolicyRenewalQuoteRequest;
import com.healthinsurance.policy.dto.request.RenewalPaymentRequest;
import com.healthinsurance.policy.dto.request.RenewalRejectRequest;
import com.healthinsurance.policy.dto.response.PolicyRenewalResponse;
import com.healthinsurance.policy.dto.response.RenewalEligibilityResponse;
import com.healthinsurance.policy.entity.Policy;
import com.healthinsurance.policy.entity.PolicyRenewal;
import com.healthinsurance.policy.enums.PolicyStatus;
import com.healthinsurance.policy.enums.RenewalStatus;
import com.healthinsurance.policy.event.PolicyEventProducer;
import com.healthinsurance.policy.event.PolicyRenewedEvent;
import com.healthinsurance.policy.exception.InvalidPolicyStateException;
import com.healthinsurance.policy.mapper.PolicyMapper;
import com.healthinsurance.policy.repository.PolicyRenewalRepository;
import com.healthinsurance.policy.repository.PolicyRepository;
import com.healthinsurance.policy.service.impl.RenewalServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
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
class RenewalServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PolicyRenewalRepository renewalRepository;

    @Mock
    private PolicyEventProducer policyEventProducer;

    private PolicyMapper policyMapper = Mappers.getMapper(PolicyMapper.class);

    private RenewalServiceImpl renewalService;

    private Policy testPolicy;
    private UUID policyId;

    @BeforeEach
    void setUp() {
        renewalService = new RenewalServiceImpl(
                policyRepository,
                renewalRepository,
                policyEventProducer,
                policyMapper
        );

        policyId = UUID.randomUUID();
        testPolicy = new Policy();
        testPolicy.setPolicyId(policyId);
        testPolicy.setPolicyNumber("POL-2026-RENEW01");
        testPolicy.setCustomerId(UUID.randomUUID());
        testPolicy.setPlanId(UUID.randomUUID());
        testPolicy.setQuoteId(UUID.randomUUID());
        testPolicy.setStatus(PolicyStatus.ACTIVE);
        // Expiry date 30 days from now (within renewal window: <= 90 days)
        testPolicy.setEffectiveDate(Instant.now().minus(335, ChronoUnit.DAYS));
        testPolicy.setExpiryDate(Instant.now().plus(30, ChronoUnit.DAYS));
        testPolicy.setMembers(new HashSet<>());
        testPolicy.setCoverages(new HashSet<>());
        testPolicy.setBeneficiaries(new HashSet<>());
        testPolicy.setRenewals(new HashSet<>());
    }

    @Test
    void testCheckEligibility_ActiveAndWithinWindow_ReturnsEligible() {
        when(policyRepository.findById(policyId)).thenReturn(Optional.of(testPolicy));

        RenewalEligibilityResponse response = renewalService.checkEligibility(policyId);

        assertNotNull(response);
        assertTrue(response.isEligible());
        assertEquals("Policy is eligible for renewal", response.getReason());
        assertTrue(response.getDaysUntilExpiry() <= 90);
    }

    @Test
    void testCheckEligibility_CancelledPolicy_ReturnsNotEligible() {
        testPolicy.setStatus(PolicyStatus.CANCELLED);
        when(policyRepository.findById(policyId)).thenReturn(Optional.of(testPolicy));

        RenewalEligibilityResponse response = renewalService.checkEligibility(policyId);

        assertNotNull(response);
        assertFalse(response.isEligible());
        assertTrue(response.getReason().contains("CANCELLED"));
    }

    @Test
    void testCheckEligibility_TooEarly_ReturnsNotEligible() {
        // Expiry in 180 days (outside the 90-day window)
        testPolicy.setExpiryDate(Instant.now().plus(180, ChronoUnit.DAYS));
        when(policyRepository.findById(policyId)).thenReturn(Optional.of(testPolicy));

        RenewalEligibilityResponse response = renewalService.checkEligibility(policyId);

        assertNotNull(response);
        assertFalse(response.isEligible());
        assertTrue(response.getReason().contains("not yet within the renewal window"));
    }

    @Test
    void testGenerateRenewalQuote_EligiblePolicy_Success() {
        when(policyRepository.findById(policyId)).thenReturn(Optional.of(testPolicy));
        when(renewalRepository.existsByPolicy_PolicyIdAndStatusIn(eq(policyId), anyCollection())).thenReturn(false);
        when(renewalRepository.save(any(PolicyRenewal.class))).thenAnswer(invocation -> {
            PolicyRenewal r = invocation.getArgument(0);
            r.setRenewalId(UUID.randomUUID());
            return r;
        });

        PolicyRenewalQuoteRequest request = PolicyRenewalQuoteRequest.builder()
                .customizedSumInsured(BigDecimal.valueOf(500000))
                .build();

        PolicyRenewalResponse response = renewalService.generateRenewalQuote(policyId, request);

        assertNotNull(response);
        assertEquals(RenewalStatus.QUOTE_GENERATED, response.getStatus());
        assertNotNull(response.getRenewalQuoteId());
        assertEquals(policyId, response.getPolicyId());
        assertEquals("POL-2026-RENEW01", response.getPolicyNumber());
        assertEquals(new BigDecimal("1000.00"), response.getRenewalPremium()); // 500000 * 0.002
        verify(renewalRepository).save(any(PolicyRenewal.class));
    }

    @Test
    void testGenerateRenewalQuote_AlreadyInProgress_ThrowsException() {
        when(policyRepository.findById(policyId)).thenReturn(Optional.of(testPolicy));
        when(renewalRepository.existsByPolicy_PolicyIdAndStatusIn(eq(policyId), anyCollection())).thenReturn(true);

        assertThrows(InvalidPolicyStateException.class, () ->
                renewalService.generateRenewalQuote(policyId, new PolicyRenewalQuoteRequest()));
    }

    @Test
    void testAcceptRenewal_Success() {
        UUID renewalId = UUID.randomUUID();
        PolicyRenewal renewal = PolicyRenewal.builder()
                .renewalId(renewalId)
                .policy(testPolicy)
                .status(RenewalStatus.QUOTE_GENERATED)
                .renewalQuoteId(UUID.randomUUID())
                .renewalPremium(BigDecimal.valueOf(1200.00))
                .build();

        when(renewalRepository.findById(renewalId)).thenReturn(Optional.of(renewal));
        when(renewalRepository.save(any(PolicyRenewal.class))).thenAnswer(i -> i.getArgument(0));

        PolicyRenewalResponse response = renewalService.acceptRenewal(policyId, renewalId);

        assertNotNull(response);
        assertEquals(RenewalStatus.PAYMENT_PENDING, response.getStatus());
        verify(renewalRepository).save(renewal);
    }

    @Test
    void testCompleteRenewal_Success_ExtendsTermAndPublishesKafka() {
        UUID renewalId = UUID.randomUUID();
        Instant newEffective = testPolicy.getExpiryDate();
        Instant newExpiry = newEffective.plus(365, ChronoUnit.DAYS);

        PolicyRenewal renewal = PolicyRenewal.builder()
                .renewalId(renewalId)
                .policy(testPolicy)
                .status(RenewalStatus.PAYMENT_PENDING)
                .renewalQuoteId(UUID.randomUUID())
                .renewalPremium(BigDecimal.valueOf(1200.00))
                .newEffectiveDate(newEffective)
                .newExpiryDate(newExpiry)
                .build();

        when(renewalRepository.findById(renewalId)).thenReturn(Optional.of(renewal));
        when(renewalRepository.save(any(PolicyRenewal.class))).thenAnswer(i -> i.getArgument(0));
        when(policyRepository.save(any(Policy.class))).thenAnswer(i -> i.getArgument(0));

        UUID paymentId = UUID.randomUUID();
        RenewalPaymentRequest request = RenewalPaymentRequest.builder()
                .paymentId(paymentId)
                .build();

        PolicyRenewalResponse response = renewalService.completeRenewal(policyId, renewalId, request);

        assertNotNull(response);
        assertEquals(RenewalStatus.COMPLETED, response.getStatus());
        assertEquals(paymentId, response.getPaymentId());

        // Verify policy dates were extended
        assertEquals(newEffective, testPolicy.getEffectiveDate());
        assertEquals(newExpiry, testPolicy.getExpiryDate());
        assertEquals(PolicyStatus.ACTIVE, testPolicy.getStatus());

        // Verify Kafka event was published
        ArgumentCaptor<PolicyRenewedEvent> captor = ArgumentCaptor.forClass(PolicyRenewedEvent.class);
        verify(policyEventProducer).publishPolicyRenewed(captor.capture());

        PolicyRenewedEvent published = captor.getValue();
        assertNotNull(published.getEventId());
        assertEquals("PolicyRenewed", published.getEventType());
        assertEquals(1, published.getEventVersion());
        assertNotNull(published.getCorrelationId());
        assertEquals(testPolicy.getPolicyId().toString(), published.getAggregateId());
        assertEquals(testPolicy.getPolicyId(), published.getPolicyId());
        assertEquals(paymentId, published.getPaymentId());
        assertNotNull(published.getTimestamp());
    }

    @Test
    void testRejectRenewal_Success() {
        UUID renewalId = UUID.randomUUID();
        PolicyRenewal renewal = PolicyRenewal.builder()
                .renewalId(renewalId)
                .policy(testPolicy)
                .status(RenewalStatus.QUOTE_GENERATED)
                .build();

        when(renewalRepository.findById(renewalId)).thenReturn(Optional.of(renewal));
        when(renewalRepository.save(any(PolicyRenewal.class))).thenAnswer(i -> i.getArgument(0));

        RenewalRejectRequest request = RenewalRejectRequest.builder()
                .rejectionReason("Customer opted for competitor policy")
                .build();

        PolicyRenewalResponse response = renewalService.rejectRenewal(policyId, renewalId, request);

        assertNotNull(response);
        assertEquals(RenewalStatus.REJECTED, response.getStatus());
        assertEquals("Customer opted for competitor policy", response.getRejectionReason());
        verify(policyRepository, never()).save(any());
        verify(policyEventProducer, never()).publishPolicyRenewed(any());
    }
}
