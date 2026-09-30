package com.healthinsurance.claims.service;

import com.healthinsurance.claims.domain.ClaimStatus;
import com.healthinsurance.claims.domain.PayeeType;
import com.healthinsurance.claims.domain.PaymentStatus;
import com.healthinsurance.claims.dto.ClaimPaymentResponse;
import com.healthinsurance.claims.dto.ClaimSettlementRequest;
import com.healthinsurance.claims.entity.Claim;
import com.healthinsurance.claims.entity.ClaimAdjudication;
import com.healthinsurance.claims.entity.ClaimPayment;
import com.healthinsurance.claims.entity.ExplanationOfBenefits;
import com.healthinsurance.claims.event.ClaimEventProducer;
import com.healthinsurance.claims.exception.InvalidClaimStateException;
import com.healthinsurance.claims.mapper.ClaimMapper;
import com.healthinsurance.claims.repository.ClaimAdjudicationRepository;
import com.healthinsurance.claims.repository.ClaimPaymentRepository;
import com.healthinsurance.claims.repository.ClaimRepository;
import com.healthinsurance.claims.repository.ExplanationOfBenefitsRepository;
import com.healthinsurance.claims.service.impl.ClaimSettlementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimSettlementServiceTest {

    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private ClaimAdjudicationRepository claimAdjudicationRepository;
    @Mock
    private ClaimPaymentRepository claimPaymentRepository;
    @Mock
    private ExplanationOfBenefitsRepository eobRepository;
    @Mock
    private ClaimService claimService;
    @Mock
    private ClaimMapper claimMapper;
    @Mock
    private ClaimEventProducer claimEventProducer;
    @Mock
    private com.healthinsurance.claims.audit.AuditTrailService auditTrailService;
    @Mock
    private com.healthinsurance.claims.metrics.ClaimMetrics claimMetrics;

    @InjectMocks
    private ClaimSettlementServiceImpl settlementService;

    private UUID claimId;
    private Claim claim;
    private ClaimSettlementRequest settlementRequest;

    @BeforeEach
    void setUp() {
        claimId = UUID.randomUUID();

        claim = new Claim();
        claim.setClaimId(claimId);
        claim.setClaimNumber("CLM-20260907-SETTLE01");
        claim.setStatus(ClaimStatus.APPROVED);
        claim.setTotalClaimAmount(new BigDecimal("10000.00"));
        claim.setApprovedAmount(new BigDecimal("8100.00"));

        settlementRequest = ClaimSettlementRequest.builder()
                .paidAmount(new BigDecimal("8100.00"))
                .payeeType(PayeeType.PROVIDER)
                .build();
    }

    @Test
    void testSettleClaim_Success() {
        ClaimAdjudication adj = new ClaimAdjudication();
        adj.setAllowedAmount(new BigDecimal("10000.00"));
        adj.setDeductibleAmount(new BigDecimal("1000.00"));
        adj.setCopayAmount(new BigDecimal("900.00"));
        adj.setCustomerResponsibility(new BigDecimal("1900.00"));

        when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));
        when(claimAdjudicationRepository.findByClaim_ClaimId(claimId)).thenReturn(Optional.of(adj));
        when(claimPaymentRepository.save(any(ClaimPayment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(eobRepository.findByClaim_ClaimId(claimId)).thenReturn(Optional.empty());
        when(eobRepository.save(any(ExplanationOfBenefits.class))).thenAnswer(inv -> inv.getArgument(0));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);
        when(claimMapper.toResponse(any(ClaimPayment.class))).thenAnswer(inv -> {
            ClaimPayment cp = inv.getArgument(0);
            return ClaimPaymentResponse.builder()
                    .claimPaymentId(UUID.randomUUID())
                    .claimId(claimId)
                    .paidAmount(cp.getPaidAmount())
                    .payeeType(cp.getPayeeType())
                    .paymentStatus(cp.getPaymentStatus())
                    .paymentReferenceNumber(cp.getPaymentReferenceNumber())
                    .build();
        });

        ClaimPaymentResponse response = settlementService.settleClaim(claimId, settlementRequest);

        assertNotNull(response);
        assertEquals(new BigDecimal("8100.00"), response.getPaidAmount());
        assertEquals(PayeeType.PROVIDER, response.getPayeeType());
        assertEquals(PaymentStatus.PROCESSED, response.getPaymentStatus());
        assertNotNull(response.getPaymentReferenceNumber());

        assertEquals(ClaimStatus.SETTLED, claim.getStatus());
        verify(claimEventProducer, times(1)).publishClaimSettled(any(Claim.class));
        verify(eobRepository, times(1)).save(any(ExplanationOfBenefits.class));
    }

    @Test
    void testSettleClaim_InvalidStatus_ThrowsException() {
        claim.setStatus(ClaimStatus.SUBMITTED);
        when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));

        assertThrows(InvalidClaimStateException.class, () ->
                settlementService.settleClaim(claimId, settlementRequest));
    }
}
