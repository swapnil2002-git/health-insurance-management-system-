package com.healthinsurance.claims.service;

import com.healthinsurance.claims.client.PolicyServiceClient;
import com.healthinsurance.claims.client.dto.PolicyClientDto;
import com.healthinsurance.claims.domain.AdjudicationDecision;
import com.healthinsurance.claims.domain.ClaimStatus;
import com.healthinsurance.claims.dto.ClaimAdjudicationResponse;
import com.healthinsurance.claims.entity.Claim;
import com.healthinsurance.claims.entity.ClaimAdjudication;
import com.healthinsurance.claims.event.ClaimEventProducer;
import com.healthinsurance.claims.mapper.ClaimMapper;
import com.healthinsurance.claims.repository.ClaimAdjudicationRepository;
import com.healthinsurance.claims.repository.ClaimRepository;
import com.healthinsurance.claims.service.impl.ClaimAdjudicationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimAdjudicationServiceTest {

    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private ClaimAdjudicationRepository claimAdjudicationRepository;
    @Mock
    private PolicyServiceClient policyServiceClient;
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
    private ClaimAdjudicationServiceImpl adjudicationService;

    private UUID claimId;
    private Claim claim;

    @BeforeEach
    void setUp() {
        claimId = UUID.randomUUID();

        claim = new Claim();
        claim.setClaimId(claimId);
        claim.setPolicyId(UUID.randomUUID());
        claim.setStatus(ClaimStatus.ADJUDICATION);
        claim.setTotalClaimAmount(new BigDecimal("10000.00"));
    }

    @Test
    void testAdjudicateClaim_ApprovedWithCalculations() {
        // Mock Policy Coverage Deductible = 1000.00
        PolicyClientDto policy = new PolicyClientDto();
        PolicyClientDto.PolicyCoverageClientDto cov = new PolicyClientDto.PolicyCoverageClientDto();
        cov.setDeductible(new BigDecimal("1000.00"));
        policy.setCoverages(List.of(cov));

        when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));
        when(policyServiceClient.getPolicyById(claim.getPolicyId())).thenReturn(policy);
        when(claimAdjudicationRepository.findByClaim_ClaimId(claimId)).thenReturn(Optional.empty());
        when(claimAdjudicationRepository.save(any(ClaimAdjudication.class))).thenAnswer(inv -> inv.getArgument(0));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);
        when(claimMapper.toResponse(any(ClaimAdjudication.class))).thenAnswer(inv -> {
            ClaimAdjudication ca = inv.getArgument(0);
            return ClaimAdjudicationResponse.builder()
                    .adjudicationId(UUID.randomUUID())
                    .claimId(claimId)
                    .submittedAmount(ca.getSubmittedAmount())
                    .allowedAmount(ca.getAllowedAmount())
                    .deductibleAmount(ca.getDeductibleAmount())
                    .copayAmount(ca.getCopayAmount())
                    .payableAmount(ca.getPayableAmount())
                    .customerResponsibility(ca.getCustomerResponsibility())
                    .decision(ca.getDecision())
                    .build();
        });

        ClaimAdjudicationResponse response = adjudicationService.adjudicateClaim(claimId);

        assertNotNull(response);
        assertEquals(AdjudicationDecision.APPROVED, response.getDecision());
        // Submitted: 10,000, Deductible: 1,000, Remaining: 9,000
        // Copay (10% of 9000): 900
        // Insurer Payable (9000 - 900): 8100.00
        // Customer Responsibility (1000 + 900): 1900.00
        assertEquals(new BigDecimal("1000.00"), response.getDeductibleAmount());
        assertEquals(new BigDecimal("900.00"), response.getCopayAmount());
        assertEquals(new BigDecimal("8100.00"), response.getPayableAmount());
        assertEquals(new BigDecimal("1900.00"), response.getCustomerResponsibility());

        assertEquals(ClaimStatus.APPROVED, claim.getStatus());
        verify(claimEventProducer, times(1)).publishClaimApproved(any(Claim.class));
    }

    @Test
    void testAdjudicateClaim_HighAmount_Referred() {
        claim.setTotalClaimAmount(new BigDecimal("60000.00")); // Above 50,000 threshold

        when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));
        when(policyServiceClient.getPolicyById(claim.getPolicyId())).thenReturn(new PolicyClientDto());
        when(claimAdjudicationRepository.findByClaim_ClaimId(claimId)).thenReturn(Optional.empty());
        when(claimAdjudicationRepository.save(any(ClaimAdjudication.class))).thenAnswer(inv -> inv.getArgument(0));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);
        when(claimMapper.toResponse(any(ClaimAdjudication.class))).thenAnswer(inv -> {
            ClaimAdjudication ca = inv.getArgument(0);
            return ClaimAdjudicationResponse.builder().decision(ca.getDecision()).build();
        });

        ClaimAdjudicationResponse response = adjudicationService.adjudicateClaim(claimId);

        assertNotNull(response);
        assertEquals(AdjudicationDecision.REFERRED, response.getDecision());
        assertEquals(ClaimStatus.REFERRED, claim.getStatus());
    }
}
