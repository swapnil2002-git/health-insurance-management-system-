package com.healthinsurance.claims.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthinsurance.claims.domain.ClaimStatus;
import com.healthinsurance.claims.domain.ClaimType;
import com.healthinsurance.claims.dto.ClaimCreateRequest;
import com.healthinsurance.claims.dto.ClaimResponse;
import com.healthinsurance.claims.entity.Claim;
import com.healthinsurance.claims.event.ClaimEventProducer;
import com.healthinsurance.claims.exception.IdempotencyConflictException;
import com.healthinsurance.claims.mapper.ClaimMapper;
import com.healthinsurance.claims.repository.ClaimRepository;
import com.healthinsurance.claims.service.impl.ClaimServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimIdempotencyTest {

    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private ClaimMapper claimMapper;
    @Mock
    private ClaimEventProducer claimEventProducer;
    @Mock
    private IdempotencyRecordRepository idempotencyRecordRepository;
    @Mock
    private com.healthinsurance.claims.audit.AuditTrailService auditTrailService;
    @Mock
    private com.healthinsurance.claims.metrics.ClaimMetrics claimMetrics;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @InjectMocks
    private ClaimServiceImpl claimService;

    private UUID claimId;
    private Claim claim;
    private ClaimCreateRequest request;
    private ClaimResponse claimResponse;
    private final String idempotencyKey = "KEY-CLAIM-12345";

    @BeforeEach
    void setUp() {
        claimId = UUID.randomUUID();

        claim = new Claim();
        claim.setClaimId(claimId);
        claim.setClaimNumber("CLM-20260909-ABCD1234");
        claim.setPolicyId(UUID.randomUUID());
        claim.setMemberId(UUID.randomUUID());
        claim.setProviderId(UUID.randomUUID());
        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setTotalClaimAmount(new BigDecimal("10000.00"));

        request = ClaimCreateRequest.builder()
                .policyId(claim.getPolicyId())
                .memberId(claim.getMemberId())
                .providerId(claim.getProviderId())
                .claimType(ClaimType.CASHLESS)
                .serviceDate(LocalDate.now())
                .totalClaimAmount(new BigDecimal("10000.00"))
                .build();

        claimResponse = ClaimResponse.builder()
                .claimId(claimId)
                .claimNumber(claim.getClaimNumber())
                .status(ClaimStatus.SUBMITTED)
                .totalClaimAmount(new BigDecimal("10000.00"))
                .build();
    }

    @Test
    void testCreateClaim_FirstRequest_SavesRecordAndPublishesEvent() {
        when(idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(idempotencyRecordRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        when(claimMapper.toEntity(any(ClaimCreateRequest.class))).thenReturn(claim);
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);
        when(claimMapper.toResponse(any(Claim.class))).thenReturn(claimResponse);

        ClaimResponse result = claimService.createClaim(request, idempotencyKey);

        assertNotNull(result);
        assertEquals(claimId, result.getClaimId());
        verify(claimRepository, times(1)).save(any(Claim.class));
        verify(claimEventProducer, times(1)).publishClaimSubmitted(any(Claim.class));
        verify(idempotencyRecordRepository, times(1)).save(any(IdempotencyRecord.class));
    }

    @Test
    void testCreateClaim_DuplicateRequest_ReturnsCachedResponseWithoutNewClaimOrEvent() throws Exception {
        String payloadJson = objectMapper.writeValueAsString(request);
        String requestHash = RequestHashUtil.computeHash(payloadJson);

        IdempotencyRecord completedRecord = IdempotencyRecord.builder()
                .idempotencyKey(idempotencyKey)
                .operationType("CLAIM_SUBMISSION")
                .requestHash(requestHash)
                .status(IdempotencyStatus.COMPLETED)
                .responseStatus(201)
                .responseBody(objectMapper.writeValueAsString(claimResponse))
                .createdAt(Instant.now())
                .build();

        when(idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(completedRecord));

        ClaimResponse result = claimService.createClaim(request, idempotencyKey);

        assertNotNull(result);
        assertEquals(claimId, result.getClaimId());
        assertEquals("CLM-20260909-ABCD1234", result.getClaimNumber());

        // Crucial verification: No second claim entity saved, NO second Kafka event published!
        verify(claimRepository, never()).save(any(Claim.class));
        verify(claimEventProducer, never()).publishClaimSubmitted(any(Claim.class));
    }

    @Test
    void testCreateClaim_SameKeyDifferentPayload_ThrowsConflictException() {
        IdempotencyRecord existingRecord = IdempotencyRecord.builder()
                .idempotencyKey(idempotencyKey)
                .operationType("CLAIM_SUBMISSION")
                .requestHash("different-sha256-hash-value")
                .status(IdempotencyStatus.COMPLETED)
                .build();

        when(idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(existingRecord));

        assertThrows(IdempotencyConflictException.class, () -> claimService.createClaim(request, idempotencyKey));
        verify(claimRepository, never()).save(any(Claim.class));
        verify(claimEventProducer, never()).publishClaimSubmitted(any(Claim.class));
    }

    @Test
    void testCreateClaim_RequestInProgress_ThrowsConflictException() throws Exception {
        String payloadJson = objectMapper.writeValueAsString(request);
        String requestHash = RequestHashUtil.computeHash(payloadJson);

        IdempotencyRecord inProgressRecord = IdempotencyRecord.builder()
                .idempotencyKey(idempotencyKey)
                .operationType("CLAIM_SUBMISSION")
                .requestHash(requestHash)
                .status(IdempotencyStatus.PROCESSING)
                .build();

        when(idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(inProgressRecord));

        assertThrows(IdempotencyConflictException.class, () -> claimService.createClaim(request, idempotencyKey));
        verify(claimRepository, never()).save(any(Claim.class));
    }
}
