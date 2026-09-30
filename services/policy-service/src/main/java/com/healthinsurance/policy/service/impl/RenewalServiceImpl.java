package com.healthinsurance.policy.service.impl;

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
import com.healthinsurance.policy.exception.PolicyNotFoundException;
import com.healthinsurance.policy.mapper.PolicyMapper;
import com.healthinsurance.policy.repository.PolicyRenewalRepository;
import com.healthinsurance.policy.repository.PolicyRepository;
import com.healthinsurance.policy.service.RenewalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RenewalServiceImpl implements RenewalService {

    private final PolicyRepository policyRepository;
    private final PolicyRenewalRepository renewalRepository;
    private final PolicyEventProducer policyEventProducer;
    private final PolicyMapper policyMapper;

    // Standard renewal window: 90 days before expiration up to 30 days after (grace period)
    private static final long RENEWAL_WINDOW_DAYS_BEFORE = 90;
    private static final long RENEWAL_GRACE_PERIOD_DAYS_AFTER = 30;

    @Override
    @Transactional(readOnly = true)
    public RenewalEligibilityResponse checkEligibility(UUID policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new PolicyNotFoundException("Policy not found with id: " + policyId));

        Instant now = Instant.now();
        Instant expiryDate = policy.getExpiryDate() != null ? policy.getExpiryDate() : now;
        long daysUntilExpiry = Duration.between(now, expiryDate).toDays();

        if (policy.getStatus() == PolicyStatus.CANCELLED) {
            return RenewalEligibilityResponse.builder()
                    .policyId(policyId)
                    .policyNumber(policy.getPolicyNumber())
                    .eligible(false)
                    .reason("Policy has been CANCELLED and cannot be renewed")
                    .currentExpiryDate(expiryDate)
                    .daysUntilExpiry(daysUntilExpiry)
                    .build();
        }

        if (daysUntilExpiry > RENEWAL_WINDOW_DAYS_BEFORE) {
            return RenewalEligibilityResponse.builder()
                    .policyId(policyId)
                    .policyNumber(policy.getPolicyNumber())
                    .eligible(false)
                    .reason("Policy is not yet within the renewal window (" + RENEWAL_WINDOW_DAYS_BEFORE + " days before expiry)")
                    .currentExpiryDate(expiryDate)
                    .daysUntilExpiry(daysUntilExpiry)
                    .build();
        }

        if (daysUntilExpiry < -RENEWAL_GRACE_PERIOD_DAYS_AFTER) {
            return RenewalEligibilityResponse.builder()
                    .policyId(policyId)
                    .policyNumber(policy.getPolicyNumber())
                    .eligible(false)
                    .reason("Policy expiry exceeds the grace period (" + RENEWAL_GRACE_PERIOD_DAYS_AFTER + " days past expiry)")
                    .currentExpiryDate(expiryDate)
                    .daysUntilExpiry(daysUntilExpiry)
                    .build();
        }

        return RenewalEligibilityResponse.builder()
                .policyId(policyId)
                .policyNumber(policy.getPolicyNumber())
                .eligible(true)
                .reason("Policy is eligible for renewal")
                .currentExpiryDate(expiryDate)
                .daysUntilExpiry(daysUntilExpiry)
                .build();
    }

    @Override
    @Transactional
    public PolicyRenewalResponse generateRenewalQuote(UUID policyId, PolicyRenewalQuoteRequest request) {
        log.info("Generating renewal quote for policy: {}", policyId);

        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new PolicyNotFoundException("Policy not found with id: " + policyId));

        RenewalEligibilityResponse eligibility = checkEligibility(policyId);
        if (!eligibility.isEligible()) {
            throw new InvalidPolicyStateException("Policy is not eligible for renewal: " + eligibility.getReason());
        }

        // Check if there is an active renewal already in flight
        if (renewalRepository.existsByPolicy_PolicyIdAndStatusIn(policyId,
                List.of(RenewalStatus.QUOTE_GENERATED, RenewalStatus.ACCEPTED, RenewalStatus.PAYMENT_PENDING))) {
            throw new InvalidPolicyStateException("A renewal quote is already in progress for this policy.");
        }

        // Calculate term renewal dates: from current expiry to +1 year
        Instant currentExpiry = policy.getExpiryDate() != null ? policy.getExpiryDate() : Instant.now();
        Instant newEffective = currentExpiry.isBefore(Instant.now()) ? Instant.now() : currentExpiry;
        Instant newExpiry = newEffective.plus(365, ChronoUnit.DAYS);

        // Standard renewal premium computation (mock baseline $1,200.00 with optional custom sum insured ratio)
        BigDecimal basePremium = BigDecimal.valueOf(1200.00).setScale(2, RoundingMode.HALF_UP);
        if (request != null && request.getCustomizedSumInsured() != null && request.getCustomizedSumInsured().compareTo(BigDecimal.ZERO) > 0) {
            basePremium = request.getCustomizedSumInsured().multiply(BigDecimal.valueOf(0.002)).setScale(2, RoundingMode.HALF_UP);
        }

        UUID renewalQuoteId = UUID.randomUUID();

        PolicyRenewal renewal = PolicyRenewal.builder()
                .policy(policy)
                .status(RenewalStatus.QUOTE_GENERATED)
                .renewalQuoteId(renewalQuoteId)
                .renewalPremium(basePremium)
                .newEffectiveDate(newEffective)
                .newExpiryDate(newExpiry)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        PolicyRenewal saved = renewalRepository.save(renewal);
        log.info("Renewal quote generated with ID: {}, quote: {}, premium: {}", saved.getRenewalId(), renewalQuoteId, basePremium);
        return policyMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PolicyRenewalResponse> getRenewalsByPolicy(UUID policyId) {
        return renewalRepository.findByPolicy_PolicyId(policyId).stream()
                .map(policyMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PolicyRenewalResponse getRenewal(UUID policyId, UUID renewalId) {
        PolicyRenewal renewal = renewalRepository.findById(renewalId)
                .filter(r -> r.getPolicy().getPolicyId().equals(policyId))
                .orElseThrow(() -> new PolicyNotFoundException("Renewal not found: " + renewalId + " for policy: " + policyId));
        return policyMapper.toResponse(renewal);
    }

    @Override
    @Transactional
    public PolicyRenewalResponse acceptRenewal(UUID policyId, UUID renewalId) {
        log.info("Accepting renewal {} for policy {}", renewalId, policyId);

        PolicyRenewal renewal = renewalRepository.findById(renewalId)
                .filter(r -> r.getPolicy().getPolicyId().equals(policyId))
                .orElseThrow(() -> new PolicyNotFoundException("Renewal not found: " + renewalId + " for policy: " + policyId));

        if (renewal.getStatus() != RenewalStatus.QUOTE_GENERATED) {
            throw new InvalidPolicyStateException("Renewal cannot be accepted from status: " + renewal.getStatus());
        }

        renewal.setStatus(RenewalStatus.PAYMENT_PENDING);
        renewal.setUpdatedAt(Instant.now());

        PolicyRenewal updated = renewalRepository.save(renewal);
        return policyMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public PolicyRenewalResponse completeRenewal(UUID policyId, UUID renewalId, RenewalPaymentRequest request) {
        log.info("Completing renewal {} with payment {}", renewalId, request.getPaymentId());

        PolicyRenewal renewal = renewalRepository.findById(renewalId)
                .filter(r -> r.getPolicy().getPolicyId().equals(policyId))
                .orElseThrow(() -> new PolicyNotFoundException("Renewal not found: " + renewalId + " for policy: " + policyId));

        if (renewal.getStatus() != RenewalStatus.PAYMENT_PENDING && renewal.getStatus() != RenewalStatus.QUOTE_GENERATED) {
            throw new InvalidPolicyStateException("Renewal cannot be completed from status: " + renewal.getStatus());
        }

        Policy policy = renewal.getPolicy();

        // Update renewal record
        renewal.setStatus(RenewalStatus.COMPLETED);
        renewal.setPaymentId(request.getPaymentId());
        renewal.setUpdatedAt(Instant.now());

        // Extend policy coverage term
        policy.setEffectiveDate(renewal.getNewEffectiveDate());
        policy.setExpiryDate(renewal.getNewExpiryDate());
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setUpdatedAt(Instant.now());

        policyRepository.save(policy);
        PolicyRenewal saved = renewalRepository.save(renewal);

        // Publish PolicyRenewedEvent
        PolicyRenewedEvent event = PolicyRenewedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("PolicyRenewed")
                .eventVersion(1)
                .timestamp(Instant.now())
                .correlationId(UUID.randomUUID().toString())
                .aggregateId(policy.getPolicyId().toString())
                .renewalId(saved.getRenewalId())
                .policyId(policy.getPolicyId())
                .policyNumber(policy.getPolicyNumber())
                .customerId(policy.getCustomerId())
                .renewalQuoteId(saved.getRenewalQuoteId())
                .renewalPremium(saved.getRenewalPremium())
                .paymentId(saved.getPaymentId())
                .newEffectiveDate(saved.getNewEffectiveDate())
                .newExpiryDate(saved.getNewExpiryDate())
                .build();

        policyEventProducer.publishPolicyRenewed(event);
        log.info("Policy {} successfully renewed through {}, event published", policyId, saved.getNewExpiryDate());

        return policyMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PolicyRenewalResponse rejectRenewal(UUID policyId, UUID renewalId, RenewalRejectRequest request) {
        log.info("Rejecting renewal {} for policy {}", renewalId, policyId);

        PolicyRenewal renewal = renewalRepository.findById(renewalId)
                .filter(r -> r.getPolicy().getPolicyId().equals(policyId))
                .orElseThrow(() -> new PolicyNotFoundException("Renewal not found: " + renewalId + " for policy: " + policyId));

        if (renewal.getStatus() == RenewalStatus.COMPLETED) {
            throw new InvalidPolicyStateException("Cannot reject an already completed renewal.");
        }

        renewal.setStatus(RenewalStatus.REJECTED);
        renewal.setRejectionReason(request.getRejectionReason());
        renewal.setUpdatedAt(Instant.now());

        PolicyRenewal updated = renewalRepository.save(renewal);
        return policyMapper.toResponse(updated);
    }
}
