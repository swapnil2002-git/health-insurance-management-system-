package com.healthinsurance.claims.service.impl;

import com.healthinsurance.claims.domain.ClaimStatus;
import com.healthinsurance.claims.domain.PaymentStatus;
import com.healthinsurance.claims.dto.ClaimPaymentResponse;
import com.healthinsurance.claims.dto.ClaimSettlementRequest;
import com.healthinsurance.claims.dto.ExplanationOfBenefitsResponse;
import com.healthinsurance.claims.entity.Claim;
import com.healthinsurance.claims.entity.ClaimAdjudication;
import com.healthinsurance.claims.entity.ClaimPayment;
import com.healthinsurance.claims.entity.ExplanationOfBenefits;
import com.healthinsurance.claims.event.ClaimEventProducer;
import com.healthinsurance.claims.exception.ClaimNotFoundException;
import com.healthinsurance.claims.exception.InvalidClaimStateException;
import com.healthinsurance.claims.mapper.ClaimMapper;
import com.healthinsurance.claims.repository.ClaimAdjudicationRepository;
import com.healthinsurance.claims.repository.ClaimPaymentRepository;
import com.healthinsurance.claims.repository.ClaimRepository;
import com.healthinsurance.claims.repository.ExplanationOfBenefitsRepository;
import com.healthinsurance.claims.service.ClaimService;
import com.healthinsurance.claims.service.ClaimSettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClaimSettlementServiceImpl implements ClaimSettlementService {

    private final ClaimRepository claimRepository;
    private final ClaimAdjudicationRepository claimAdjudicationRepository;
    private final ClaimPaymentRepository claimPaymentRepository;
    private final ExplanationOfBenefitsRepository eobRepository;
    private final ClaimService claimService;
    private final ClaimMapper claimMapper;
    private final ClaimEventProducer claimEventProducer;
    private final com.healthinsurance.claims.audit.AuditTrailService auditTrailService;
    private final com.healthinsurance.claims.metrics.ClaimMetrics claimMetrics;

    @Override
    public ClaimPaymentResponse settleClaim(UUID claimId, ClaimSettlementRequest request) {
        log.info("Processing settlement for claimId: {}, amount: {}", claimId, request.getPaidAmount());
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ClaimNotFoundException("Claim not found with ID: " + claimId));

        if (claim.getStatus() != ClaimStatus.APPROVED && claim.getStatus() != ClaimStatus.PAYMENT_PENDING) {
            throw new InvalidClaimStateException("Claim must be APPROVED or PAYMENT_PENDING before settlement. Current status: " + claim.getStatus());
        }

        // Transition through PAYMENT_PENDING -> SETTLED
        claimService.validateStateTransition(claim.getStatus(), ClaimStatus.PAYMENT_PENDING);
        claim.setStatus(ClaimStatus.PAYMENT_PENDING);

        claimService.validateStateTransition(claim.getStatus(), ClaimStatus.SETTLED);
        claim.setStatus(ClaimStatus.SETTLED);

        // Fetch adjudication details to construct EOB accurately
        ClaimAdjudication adjudication = claimAdjudicationRepository.findByClaim_ClaimId(claimId)
                .orElse(null);

        // Generate Payment Record
        String paymentRef = (request.getPaymentReferenceNumber() != null && !request.getPaymentReferenceNumber().isBlank())
                ? request.getPaymentReferenceNumber()
                : generatePaymentReference();

        ClaimPayment payment = new ClaimPayment();
        payment.setClaim(claim);
        payment.setPaidAmount(request.getPaidAmount());
        payment.setPayeeType(request.getPayeeType());
        payment.setPaymentReferenceNumber(paymentRef);
        payment.setPaymentStatus(PaymentStatus.PROCESSED);
        payment.setSettledAt(Instant.now());

        claim.addPayment(payment);
        ClaimPayment savedPayment = claimPaymentRepository.save(payment);

        // Generate Explanation of Benefits (EOB) if not already generated
        ExplanationOfBenefits existingEob = eobRepository.findByClaim_ClaimId(claimId).orElse(null);
        if (existingEob == null) {
            ExplanationOfBenefits eob = new ExplanationOfBenefits();
            eob.setClaim(claim);
            eob.setEobNumber(generateEobNumber());
            eob.setClaimAmount(claim.getTotalClaimAmount() != null ? claim.getTotalClaimAmount() : BigDecimal.ZERO);
            eob.setAllowedAmount(adjudication != null ? adjudication.getAllowedAmount() : claim.getTotalClaimAmount());
            eob.setDeductible(adjudication != null ? adjudication.getDeductibleAmount() : BigDecimal.ZERO);
            eob.setCopay(adjudication != null ? adjudication.getCopayAmount() : BigDecimal.ZERO);
            eob.setInsurancePayment(request.getPaidAmount());
            eob.setCustomerResponsibility(adjudication != null ? adjudication.getCustomerResponsibility() : BigDecimal.ZERO);
            eob.setRemarks("Claim settled successfully. Payment issued via reference: " + paymentRef);
            eob.setGeneratedAt(Instant.now());

            claim.setExplanationOfBenefits(eob);
            eobRepository.save(eob);
            log.info("EOB created with number: {} for claim: {}", eob.getEobNumber(), claim.getClaimNumber());
        }

        Claim savedClaim = claimRepository.save(claim);
        log.info("Claim {} successfully settled with payment reference {}", claimId, paymentRef);

        // Record Audit Trail (Before vs After)
        auditTrailService.recordAudit(
                "CLAIM_SETTLED",
                "Claim",
                savedClaim.getClaimId().toString(),
                java.util.Map.of("status", "APPROVED"),
                java.util.Map.of("status", "SETTLED", "paidAmount", request.getPaidAmount(), "paymentReference", paymentRef),
                "/api/claims/" + claimId + "/settle",
                null
        );

        // Record Business Metrics
        claimMetrics.recordClaimSettled(request.getPayeeType() != null ? request.getPayeeType().name() : "PROVIDER");
        if (savedClaim.getCreatedAt() != null) {
            java.time.Duration totalDuration = java.time.Duration.between(savedClaim.getCreatedAt(), java.time.Instant.now());
            claimMetrics.recordClaimProcessingDuration(totalDuration);
        }

        // Publish CLAIM_SETTLED event to Kafka
        claimEventProducer.publishClaimSettled(savedClaim);

        return claimMapper.toResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public ExplanationOfBenefitsResponse getEobByClaimId(UUID claimId) {
        log.info("Fetching EOB for claimId: {}", claimId);
        ExplanationOfBenefits eob = eobRepository.findByClaim_ClaimId(claimId)
                .orElseThrow(() -> new ClaimNotFoundException("EOB not found for claim ID: " + claimId));
        return claimMapper.toResponse(eob);
    }

    @Override
    @Transactional(readOnly = true)
    public ExplanationOfBenefitsResponse getEobByNumber(String eobNumber) {
        log.info("Fetching EOB for eobNumber: {}", eobNumber);
        ExplanationOfBenefits eob = eobRepository.findByEobNumber(eobNumber)
                .orElseThrow(() -> new ClaimNotFoundException("EOB not found with number: " + eobNumber));
        return claimMapper.toResponse(eob);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaimPaymentResponse> getPaymentsByClaimId(UUID claimId) {
        log.info("Fetching payment history for claimId: {}", claimId);
        return claimPaymentRepository.findByClaim_ClaimId(claimId).stream()
                .map(claimMapper::toResponse)
                .collect(Collectors.toList());
    }

    private String generatePaymentReference() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String rand = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "PAY-" + date + "-" + rand;
    }

    private String generateEobNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String rand = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "EOB-" + date + "-" + rand;
    }
}
