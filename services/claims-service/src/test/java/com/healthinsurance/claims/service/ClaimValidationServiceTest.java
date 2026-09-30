package com.healthinsurance.claims.service;

import com.healthinsurance.claims.domain.ClaimStatus;
import com.healthinsurance.claims.domain.ClaimType;
import com.healthinsurance.claims.dto.ClaimValidationResponse;
import com.healthinsurance.claims.entity.Claim;
import com.healthinsurance.claims.entity.ClaimDiagnosis;
import com.healthinsurance.claims.entity.ClaimValidation;
import com.healthinsurance.claims.event.ClaimEventProducer;
import com.healthinsurance.claims.exception.ClaimValidationException;
import com.healthinsurance.claims.mapper.ClaimMapper;
import com.healthinsurance.claims.repository.ClaimRepository;
import com.healthinsurance.claims.repository.ClaimValidationRepository;
import com.healthinsurance.claims.service.impl.ClaimValidationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimValidationServiceTest {

    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private ClaimValidationRepository claimValidationRepository;
    @Mock
    private ClaimService claimService;
    @Mock
    private ClaimMapper claimMapper;
    @Mock
    private ClaimEventProducer claimEventProducer;

    @InjectMocks
    private ClaimValidationServiceImpl claimValidationService;

    private UUID claimId;
    private Claim claim;

    @BeforeEach
    void setUp() {
        claimId = UUID.randomUUID();

        claim = new Claim();
        claim.setClaimId(claimId);
        claim.setClaimNumber("CLM-20260907-VAL0001");
        claim.setPolicyId(UUID.randomUUID());
        claim.setMemberId(UUID.randomUUID());
        claim.setProviderId(UUID.randomUUID());
        claim.setClaimType(ClaimType.CASHLESS);
        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setTotalClaimAmount(new BigDecimal("10000.00"));
        claim.setServiceDate(LocalDate.now().minusDays(1));

        // Add 1 service line
        com.healthinsurance.claims.entity.ClaimService line = new com.healthinsurance.claims.entity.ClaimService();
        line.setServiceCode("SRV-001");
        line.setServiceDescription("Consultation");
        line.setServiceDate(LocalDate.now().minusDays(1));
        line.setUnitPrice(new BigDecimal("10000.00"));
        line.setQuantity(1);
        line.setTotalAmount(new BigDecimal("10000.00"));
        claim.addServiceLine(line);

        // Add 1 diagnosis
        ClaimDiagnosis diag = new ClaimDiagnosis();
        diag.setDiagnosisCode("J00");
        claim.addDiagnosis(diag);
    }

    @Test
    void testValidateClaim_Success() {
        when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));
        when(claimRepository.existsByPolicyIdAndMemberIdAndServiceDateAndStatusNot(any(), any(), any(), any()))
                .thenReturn(false);
        when(claimValidationRepository.saveAll(any())).thenReturn(List.of());
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);
        when(claimMapper.toResponse(any(ClaimValidation.class))).thenReturn(ClaimValidationResponse.builder().build());

        List<ClaimValidationResponse> results = claimValidationService.validateClaim(claimId);

        assertNotNull(results);
        assertEquals(5, results.size());
        verify(claimEventProducer, times(1)).publishClaimValidated(any(Claim.class));
    }

    @Test
    void testValidateClaim_FutureServiceDate_ThrowsValidationException() {
        claim.setServiceDate(LocalDate.now().plusDays(2));
        when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));
        when(claimRepository.existsByPolicyIdAndMemberIdAndServiceDateAndStatusNot(any(), any(), any(), any()))
                .thenReturn(false);
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);

        assertThrows(ClaimValidationException.class, () -> claimValidationService.validateClaim(claimId));
        verify(claimEventProducer, times(1)).publishClaimRejected(any(Claim.class), anyString());
    }

    @Test
    void testValidateClaim_ZeroAmount_ThrowsValidationException() {
        claim.setTotalClaimAmount(BigDecimal.ZERO);
        when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));
        when(claimRepository.existsByPolicyIdAndMemberIdAndServiceDateAndStatusNot(any(), any(), any(), any()))
                .thenReturn(false);
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);

        assertThrows(ClaimValidationException.class, () -> claimValidationService.validateClaim(claimId));
    }
}
