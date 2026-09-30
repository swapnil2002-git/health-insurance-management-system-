package com.healthinsurance.claims.service;

import com.healthinsurance.claims.domain.ClaimStatus;
import com.healthinsurance.claims.domain.ClaimType;
import com.healthinsurance.claims.dto.ClaimCreateRequest;
import com.healthinsurance.claims.dto.ClaimResponse;
import com.healthinsurance.claims.entity.Claim;
import com.healthinsurance.claims.event.ClaimEventProducer;
import com.healthinsurance.claims.exception.ClaimNotFoundException;
import com.healthinsurance.claims.exception.InvalidClaimStateException;
import com.healthinsurance.claims.mapper.ClaimMapper;
import com.healthinsurance.claims.repository.ClaimDiagnosisRepository;
import com.healthinsurance.claims.repository.ClaimDocumentRepository;
import com.healthinsurance.claims.repository.ClaimRepository;
import com.healthinsurance.claims.repository.ClaimServiceRepository;
import com.healthinsurance.claims.service.impl.ClaimServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private ClaimServiceRepository claimServiceRepository;
    @Mock
    private ClaimDiagnosisRepository claimDiagnosisRepository;
    @Mock
    private ClaimDocumentRepository claimDocumentRepository;
    @Mock
    private ClaimMapper claimMapper;
    @Mock
    private ClaimEventProducer claimEventProducer;
    @Mock
    private com.healthinsurance.claims.audit.AuditTrailService auditTrailService;
    @Mock
    private com.healthinsurance.claims.metrics.ClaimMetrics claimMetrics;

    @InjectMocks
    private ClaimServiceImpl claimService;

    private UUID claimId;
    private Claim claim;
    private ClaimCreateRequest createRequest;
    private ClaimResponse claimResponse;

    @BeforeEach
    void setUp() {
        claimId = UUID.randomUUID();

        claim = new Claim();
        claim.setClaimId(claimId);
        claim.setClaimNumber("CLM-20260907-TEST0001");
        claim.setPolicyId(UUID.randomUUID());
        claim.setMemberId(UUID.randomUUID());
        claim.setProviderId(UUID.randomUUID());
        claim.setClaimType(ClaimType.CASHLESS);
        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setTotalClaimAmount(new BigDecimal("15000.00"));
        claim.setServiceDate(LocalDate.now());

        createRequest = ClaimCreateRequest.builder()
                .policyId(claim.getPolicyId())
                .memberId(claim.getMemberId())
                .providerId(claim.getProviderId())
                .claimType(ClaimType.CASHLESS)
                .totalClaimAmount(new BigDecimal("15000.00"))
                .serviceDate(LocalDate.now())
                .build();

        claimResponse = ClaimResponse.builder()
                .claimId(claimId)
                .claimNumber(claim.getClaimNumber())
                .status(ClaimStatus.SUBMITTED)
                .totalClaimAmount(new BigDecimal("15000.00"))
                .build();
    }

    @Test
    void testCreateClaim_Success() {
        when(claimMapper.toEntity(any(ClaimCreateRequest.class))).thenReturn(claim);
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);
        when(claimMapper.toResponse(any(Claim.class))).thenReturn(claimResponse);

        ClaimResponse result = claimService.createClaim(createRequest);

        assertNotNull(result);
        assertEquals(claimId, result.getClaimId());
        assertEquals(ClaimStatus.SUBMITTED, result.getStatus());
        verify(claimRepository, times(1)).save(any(Claim.class));
        verify(claimEventProducer, times(1)).publishClaimSubmitted(any(Claim.class));
    }

    @Test
    void testGetClaimById_Success() {
        when(claimRepository.findById(claimId)).thenReturn(Optional.of(claim));
        when(claimMapper.toResponse(claim)).thenReturn(claimResponse);

        ClaimResponse result = claimService.getClaimById(claimId);

        assertNotNull(result);
        assertEquals(claimId, result.getClaimId());
    }

    @Test
    void testGetClaimById_NotFound_ThrowsException() {
        when(claimRepository.findById(claimId)).thenReturn(Optional.empty());

        assertThrows(ClaimNotFoundException.class, () -> claimService.getClaimById(claimId));
    }

    @Test
    void testValidateStateTransition_Valid() {
        assertDoesNotThrow(() -> claimService.validateStateTransition(ClaimStatus.SUBMITTED, ClaimStatus.VALIDATING));
        assertDoesNotThrow(() -> claimService.validateStateTransition(ClaimStatus.VALIDATING, ClaimStatus.ELIGIBILITY_CHECK));
        assertDoesNotThrow(() -> claimService.validateStateTransition(ClaimStatus.APPROVED, ClaimStatus.PAYMENT_PENDING));
        assertDoesNotThrow(() -> claimService.validateStateTransition(ClaimStatus.PAYMENT_PENDING, ClaimStatus.SETTLED));
    }

    @Test
    void testValidateStateTransition_Invalid_ThrowsException() {
        assertThrows(InvalidClaimStateException.class, () ->
                claimService.validateStateTransition(ClaimStatus.SUBMITTED, ClaimStatus.SETTLED));
        assertThrows(InvalidClaimStateException.class, () ->
                claimService.validateStateTransition(ClaimStatus.SETTLED, ClaimStatus.VALIDATING));
    }
}
