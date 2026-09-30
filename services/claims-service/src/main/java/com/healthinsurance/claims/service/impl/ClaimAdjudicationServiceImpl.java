package com.healthinsurance.claims.service.impl;

import com.healthinsurance.claims.client.PolicyServiceClient;
import com.healthinsurance.claims.client.dto.PolicyClientDto;
import com.healthinsurance.claims.domain.AdjudicationDecision;
import com.healthinsurance.claims.domain.ClaimStatus;
import com.healthinsurance.claims.dto.ClaimAdjudicationResponse;
import com.healthinsurance.claims.entity.Claim;
import com.healthinsurance.claims.entity.ClaimAdjudication;
import com.healthinsurance.claims.event.ClaimEventProducer;
import com.healthinsurance.claims.exception.ClaimNotFoundException;
import com.healthinsurance.claims.exception.InvalidClaimStateException;
import com.healthinsurance.claims.mapper.ClaimMapper;
import com.healthinsurance.claims.repository.ClaimAdjudicationRepository;
import com.healthinsurance.claims.repository.ClaimRepository;
import com.healthinsurance.claims.service.ClaimAdjudicationService;
import com.healthinsurance.claims.service.ClaimService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClaimAdjudicationServiceImpl implements ClaimAdjudicationService {

    private final ClaimRepository claimRepository;
    private final ClaimAdjudicationRepository claimAdjudicationRepository;
    private final PolicyServiceClient policyServiceClient;
    private final ClaimService claimService;
    private final ClaimMapper claimMapper;
    private final ClaimEventProducer claimEventProducer;
    private final com.healthinsurance.claims.audit.AuditTrailService auditTrailService;
    private final com.healthinsurance.claims.metrics.ClaimMetrics claimMetrics;

    // Threshold above which claims require manual review / referral
    private static final BigDecimal REFERRAL_THRESHOLD = new BigDecimal("50000.00");
    private static final BigDecimal DEFAULT_COPAY_PERCENTAGE = new BigDecimal("10.00"); // 10% copay

    @Override
    public ClaimAdjudicationResponse adjudicateClaim(UUID claimId) {
        log.info("Adjudicating claimId: {}", claimId);
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ClaimNotFoundException("Claim not found with ID: " + claimId));

        // Ensure claim is in a valid state for adjudication
        if (claim.getStatus() != ClaimStatus.ADJUDICATION && claim.getStatus() != ClaimStatus.REFERRED) {
            claimService.validateStateTransition(claim.getStatus(), ClaimStatus.ADJUDICATION);
            claim.setStatus(ClaimStatus.ADJUDICATION);
        }

        BigDecimal submittedAmount = claim.getTotalClaimAmount() != null ? claim.getTotalClaimAmount() : BigDecimal.ZERO;
        BigDecimal allowedAmount = submittedAmount; // Allowed by default in network
        BigDecimal deductible = BigDecimal.ZERO;
        BigDecimal copayPercentage = DEFAULT_COPAY_PERCENTAGE;

        // Fetch policy coverage details via OpenFeign
        try {
            PolicyClientDto policy = policyServiceClient.getPolicyById(claim.getPolicyId());
            if (policy.getCoverages() != null && !policy.getCoverages().isEmpty()) {
                // Sum or pick deductible from applicable coverage
                deductible = policy.getCoverages().stream()
                        .map(c -> c.getDeductible() != null ? c.getDeductible() : BigDecimal.ZERO)
                        .filter(d -> d.compareTo(BigDecimal.ZERO) > 0)
                        .findFirst()
                        .orElse(BigDecimal.ZERO);
            }
        } catch (Exception ex) {
            log.warn("Could not fetch coverage deductible from policy-service for policy {}: {}", claim.getPolicyId(), ex.getMessage());
        }

        // Apply deductible (deductible cannot exceed allowed amount)
        BigDecimal deductibleApplied = deductible.min(allowedAmount);
        BigDecimal remainingAfterDeductible = allowedAmount.subtract(deductibleApplied);

        // Calculate Copay
        BigDecimal copayAmount = remainingAfterDeductible
                .multiply(copayPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Calculate Payable Amount (Insurer pays)
        BigDecimal payableAmount = remainingAfterDeductible.subtract(copayAmount).max(BigDecimal.ZERO);

        // Customer Responsibility = Deductible + Copay
        BigDecimal customerResponsibility = deductibleApplied.add(copayAmount);

        // Decision logic
        AdjudicationDecision decision;
        String reason;

        if (submittedAmount.compareTo(REFERRAL_THRESHOLD) > 0) {
            decision = AdjudicationDecision.REFERRED;
            reason = "Claim total exceeds auto-adjudication threshold of " + REFERRAL_THRESHOLD + ". Referred to medical adjuster.";
            claim.setStatus(ClaimStatus.REFERRED);
        } else if (payableAmount.compareTo(BigDecimal.ZERO) > 0) {
            decision = AdjudicationDecision.APPROVED;
            reason = "Claim approved after deductible and copay calculations.";
            claim.setStatus(ClaimStatus.APPROVED);
            claim.setApprovedAmount(payableAmount);
        } else {
            decision = AdjudicationDecision.REJECTED;
            reason = "Claim covered amount fully exhausted by deductible or zero payable.";
            claim.setStatus(ClaimStatus.REJECTED);
            claim.setApprovedAmount(BigDecimal.ZERO);
        }

        // Check if an adjudication record already exists (e.g. re-adjudication)
        ClaimAdjudication adjudication = claimAdjudicationRepository.findByClaim_ClaimId(claimId)
                .orElse(new ClaimAdjudication());

        adjudication.setClaim(claim);
        adjudication.setSubmittedAmount(submittedAmount);
        adjudication.setAllowedAmount(allowedAmount);
        adjudication.setDeductibleAmount(deductibleApplied);
        adjudication.setCopayAmount(copayAmount);
        adjudication.setCopayPercentage(copayPercentage);
        adjudication.setPayableAmount(payableAmount);
        adjudication.setCustomerResponsibility(customerResponsibility);
        adjudication.setDecision(decision);
        adjudication.setReason(reason);
        adjudication.setAdjudicatedAt(Instant.now());

        ClaimAdjudication savedAdjudication = claimAdjudicationRepository.save(adjudication);
        Claim savedClaim = claimRepository.save(claim);

        log.info("Claim {} adjudicated with decision: {}, payable: {}", claimId, decision, payableAmount);

        // Record Business Metrics
        claimMetrics.recordClaimAdjudicated(decision.name());
        if (savedClaim.getCreatedAt() != null) {
            java.time.Duration duration = java.time.Duration.between(savedClaim.getCreatedAt(), java.time.Instant.now());
            claimMetrics.recordAdjudicationDuration(duration, decision.name());
        }

        // Record Audit Trail (Before vs After)
        String opType = (decision == AdjudicationDecision.APPROVED) ? "CLAIM_APPROVED" : "CLAIM_REJECTED";
        auditTrailService.recordAudit(
                opType,
                "Claim",
                savedClaim.getClaimId().toString(),
                java.util.Map.of("status", "SUBMITTED"),
                java.util.Map.of("status", savedClaim.getStatus().name(), "decision", decision.name(), "approvedAmount", savedClaim.getApprovedAmount(), "reason", reason),
                "/api/claims/" + claimId + "/adjudicate",
                null
        );

        // Publish appropriate Kafka events
        if (decision == AdjudicationDecision.APPROVED) {
            claimEventProducer.publishClaimApproved(savedClaim);
        } else if (decision == AdjudicationDecision.REJECTED) {
            claimEventProducer.publishClaimRejected(savedClaim, reason);
        }

        return claimMapper.toResponse(savedAdjudication);
    }

    @Override
    @Transactional(readOnly = true)
    public ClaimAdjudicationResponse getAdjudicationByClaimId(UUID claimId) {
        log.info("Fetching adjudication record for claimId: {}", claimId);
        ClaimAdjudication adjudication = claimAdjudicationRepository.findByClaim_ClaimId(claimId)
                .orElseThrow(() -> new ClaimNotFoundException("Adjudication record not found for claim ID: " + claimId));
        return claimMapper.toResponse(adjudication);
    }
}
