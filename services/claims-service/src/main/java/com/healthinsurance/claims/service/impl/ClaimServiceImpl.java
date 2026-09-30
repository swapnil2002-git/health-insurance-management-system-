package com.healthinsurance.claims.service.impl;

import com.healthinsurance.claims.domain.ClaimStatus;
import com.healthinsurance.claims.dto.*;
import com.healthinsurance.claims.entity.Claim;
import com.healthinsurance.claims.entity.ClaimDiagnosis;
import com.healthinsurance.claims.entity.ClaimDocument;
import com.healthinsurance.claims.event.ClaimEventProducer;
import com.healthinsurance.claims.exception.ClaimNotFoundException;
import com.healthinsurance.claims.exception.InvalidClaimStateException;
import com.healthinsurance.claims.mapper.ClaimMapper;
import com.healthinsurance.claims.repository.*;
import com.healthinsurance.claims.service.ClaimService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthinsurance.claims.exception.IdempotencyConflictException;
import com.healthinsurance.claims.idempotency.IdempotencyRecord;
import com.healthinsurance.claims.idempotency.IdempotencyRecordRepository;
import com.healthinsurance.claims.idempotency.IdempotencyStatus;
import com.healthinsurance.claims.idempotency.RequestHashUtil;
import org.springframework.dao.DataIntegrityViolationException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClaimServiceImpl implements ClaimService {

    private final ClaimRepository claimRepository;
    private final ClaimServiceRepository claimServiceRepository;
    private final ClaimDiagnosisRepository claimDiagnosisRepository;
    private final ClaimDocumentRepository claimDocumentRepository;
    private final ClaimMapper claimMapper;
    private final ClaimEventProducer claimEventProducer;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final ObjectMapper objectMapper;
    private final com.healthinsurance.claims.audit.AuditTrailService auditTrailService;
    private final com.healthinsurance.claims.metrics.ClaimMetrics claimMetrics;

    // Defined allowed transitions per specification
    private static final Map<ClaimStatus, Set<ClaimStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(ClaimStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(ClaimStatus.SUBMITTED, EnumSet.of(ClaimStatus.VALIDATING, ClaimStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(ClaimStatus.VALIDATING, EnumSet.of(ClaimStatus.ELIGIBILITY_CHECK, ClaimStatus.REJECTED, ClaimStatus.REFERRED));
        ALLOWED_TRANSITIONS.put(ClaimStatus.ELIGIBILITY_CHECK, EnumSet.of(ClaimStatus.PROVIDER_CHECK, ClaimStatus.REJECTED, ClaimStatus.REFERRED));
        ALLOWED_TRANSITIONS.put(ClaimStatus.PROVIDER_CHECK, EnumSet.of(ClaimStatus.ADJUDICATION, ClaimStatus.REJECTED, ClaimStatus.REFERRED));
        ALLOWED_TRANSITIONS.put(ClaimStatus.ADJUDICATION, EnumSet.of(ClaimStatus.APPROVED, ClaimStatus.REJECTED, ClaimStatus.REFERRED));
        ALLOWED_TRANSITIONS.put(ClaimStatus.APPROVED, EnumSet.of(ClaimStatus.PAYMENT_PENDING));
        ALLOWED_TRANSITIONS.put(ClaimStatus.PAYMENT_PENDING, EnumSet.of(ClaimStatus.SETTLED));
        ALLOWED_TRANSITIONS.put(ClaimStatus.REFERRED, EnumSet.of(ClaimStatus.ADJUDICATION, ClaimStatus.APPROVED, ClaimStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(ClaimStatus.REJECTED, EnumSet.noneOf(ClaimStatus.class));
        ALLOWED_TRANSITIONS.put(ClaimStatus.SETTLED, EnumSet.noneOf(ClaimStatus.class));
    }

    @Override
    public ClaimResponse createClaim(ClaimCreateRequest request) {
        return createClaim(request, null);
    }

    @Override
    public ClaimResponse createClaim(ClaimCreateRequest request, String idempotencyKey) {
        log.info("Creating claim for policyId: {}, memberId: {}, idempotencyKey: {}", 
                request.getPolicyId(), request.getMemberId(), idempotencyKey);

        IdempotencyRecord existingRecord = null;
        String requestHash = null;

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            try {
                String payloadJson = objectMapper.writeValueAsString(request);
                requestHash = RequestHashUtil.computeHash(payloadJson);
            } catch (Exception e) {
                requestHash = RequestHashUtil.computeHash(request.toString());
            }

            Optional<IdempotencyRecord> recordOpt = idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey);
            if (recordOpt.isPresent()) {
                existingRecord = recordOpt.get();
                if (!existingRecord.getRequestHash().equals(requestHash)) {
                    log.warn("Idempotency key {} reused with different payload!", idempotencyKey);
                    throw new IdempotencyConflictException("Idempotency key '" + idempotencyKey + "' has already been used with a different request payload.");
                }
                if (existingRecord.getStatus() == IdempotencyStatus.COMPLETED && existingRecord.getResponseBody() != null) {
                    log.info("Returning cached idempotent response for key: {}", idempotencyKey);
                    try {
                        return objectMapper.readValue(existingRecord.getResponseBody(), ClaimResponse.class);
                    } catch (Exception e) {
                        log.error("Failed to deserialize cached response body for idempotency key: {}", idempotencyKey, e);
                    }
                }
                if (existingRecord.getStatus() == IdempotencyStatus.PROCESSING) {
                    throw new IdempotencyConflictException("A request with idempotency key '" + idempotencyKey + "' is currently in progress. Please retry shortly.");
                }
            } else {
                // Pre-persist processing record to acquire DB-level unique lock
                IdempotencyRecord newRecord = IdempotencyRecord.builder()
                        .idempotencyKey(idempotencyKey)
                        .operationType("CLAIM_SUBMISSION")
                        .requestHash(requestHash)
                        .status(IdempotencyStatus.PROCESSING)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();
                try {
                    existingRecord = idempotencyRecordRepository.saveAndFlush(newRecord);
                } catch (DataIntegrityViolationException dive) {
                    // Concurrent race condition: Another thread inserted the key simultaneously
                    existingRecord = idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey)
                            .orElseThrow(() -> new IdempotencyConflictException("Concurrent duplicate request detected for key: " + idempotencyKey));
                    if (!existingRecord.getRequestHash().equals(requestHash)) {
                        throw new IdempotencyConflictException("Idempotency key '" + idempotencyKey + "' has already been used with a different request payload.");
                    }
                    if (existingRecord.getStatus() == IdempotencyStatus.COMPLETED && existingRecord.getResponseBody() != null) {
                        try {
                            return objectMapper.readValue(existingRecord.getResponseBody(), ClaimResponse.class);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to read cached idempotent claim response", e);
                        }
                    }
                    throw new IdempotencyConflictException("A request with idempotency key '" + idempotencyKey + "' is currently in progress.");
                }
            }
        }

        Claim claim = claimMapper.toEntity(request);
        claim.setClaimNumber(generateClaimNumber());
        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setApprovedAmount(BigDecimal.ZERO);

        // Populate service lines if provided
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        if (request.getServiceLines() != null && !request.getServiceLines().isEmpty()) {
            for (ClaimServiceRequest lineReq : request.getServiceLines()) {
                com.healthinsurance.claims.entity.ClaimService serviceLine = claimMapper.toEntity(lineReq);
                int qty = (lineReq.getQuantity() != null && lineReq.getQuantity() > 0) ? lineReq.getQuantity() : 1;
                serviceLine.setQuantity(qty);
                BigDecimal lineTotal = lineReq.getUnitPrice().multiply(BigDecimal.valueOf(qty));
                serviceLine.setTotalAmount(lineTotal);
                claim.addServiceLine(serviceLine);
                calculatedTotal = calculatedTotal.add(lineTotal);
            }
        }

        // Set or override total claim amount from lines if lines exist
        if (calculatedTotal.compareTo(BigDecimal.ZERO) > 0) {
            claim.setTotalClaimAmount(calculatedTotal);
        }

        // Populate diagnoses if provided
        if (request.getDiagnoses() != null && !request.getDiagnoses().isEmpty()) {
            for (ClaimDiagnosisRequest diagReq : request.getDiagnoses()) {
                ClaimDiagnosis diagnosis = claimMapper.toEntity(diagReq);
                claim.addDiagnosis(diagnosis);
            }
        }

        Claim saved = claimRepository.save(claim);
        log.info("Claim created successfully with claimNumber: {} and ID: {}", saved.getClaimNumber(), saved.getClaimId());

        // Publish ClaimSubmitted event
        claimEventProducer.publishClaimSubmitted(saved);

        // Record Business Metric
        claimMetrics.recordClaimSubmitted(saved.getClaimType() != null ? saved.getClaimType().name() : "GENERAL");

        ClaimResponse response = claimMapper.toResponse(saved);

        // Record Audit Trail (Before: null, After: status=SUBMITTED)
        auditTrailService.recordAudit(
                "CLAIM_SUBMITTED",
                "Claim",
                saved.getClaimId().toString(),
                null,
                Map.of("claimNumber", saved.getClaimNumber(), "status", saved.getStatus().name(), "amount", saved.getTotalClaimAmount()),
                "/api/claims",
                null
        );

        // Update idempotency record to COMPLETED with serialized response
        if (existingRecord != null) {
            try {
                existingRecord.setStatus(IdempotencyStatus.COMPLETED);
                existingRecord.setResourceId(saved.getClaimId().toString());
                existingRecord.setResponseStatus(201);
                existingRecord.setResponseBody(objectMapper.writeValueAsString(response));
                existingRecord.setUpdatedAt(Instant.now());
                idempotencyRecordRepository.save(existingRecord);
            } catch (Exception ex) {
                log.error("Failed to store serialized idempotency response for key: {}", idempotencyKey, ex);
            }
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ClaimResponse getClaimById(UUID claimId) {
        log.info("Fetching claim by ID: {}", claimId);
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ClaimNotFoundException("Claim not found with ID: " + claimId));
        return claimMapper.toResponse(claim);
    }

    @Override
    @Transactional(readOnly = true)
    public ClaimResponse getClaimByNumber(String claimNumber) {
        log.info("Fetching claim by claimNumber: {}", claimNumber);
        Claim claim = claimRepository.findByClaimNumber(claimNumber)
                .orElseThrow(() -> new ClaimNotFoundException("Claim not found with number: " + claimNumber));
        return claimMapper.toResponse(claim);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaimResponse> getAllClaims(ClaimStatus status) {
        log.info("Fetching all claims with status filter: {}", status);
        List<Claim> claims = (status != null) ? claimRepository.findByStatus(status) : claimRepository.findAll();
        return claimMapper.toClaimResponseList(claims);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaimResponse> getClaimsByPolicyId(UUID policyId) {
        log.info("Fetching claims for policyId: {}", policyId);
        return claimMapper.toClaimResponseList(claimRepository.findByPolicyId(policyId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaimResponse> getClaimsByMemberId(UUID memberId) {
        log.info("Fetching claims for memberId: {}", memberId);
        return claimMapper.toClaimResponseList(claimRepository.findByMemberId(memberId));
    }

    @Override
    public ClaimServiceResponse addServiceLine(UUID claimId, ClaimServiceRequest request) {
        log.info("Adding service line to claimId: {}", claimId);
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ClaimNotFoundException("Claim not found with ID: " + claimId));

        if (claim.getStatus() == ClaimStatus.SETTLED || claim.getStatus() == ClaimStatus.REJECTED) {
            throw new InvalidClaimStateException("Cannot add service lines to claim in state: " + claim.getStatus());
        }

        com.healthinsurance.claims.entity.ClaimService serviceLine = claimMapper.toEntity(request);
        int qty = (request.getQuantity() != null && request.getQuantity() > 0) ? request.getQuantity() : 1;
        serviceLine.setQuantity(qty);
        BigDecimal lineTotal = request.getUnitPrice().multiply(BigDecimal.valueOf(qty));
        serviceLine.setTotalAmount(lineTotal);

        claim.addServiceLine(serviceLine);
        claim.setTotalClaimAmount(claim.getTotalClaimAmount().add(lineTotal));

        com.healthinsurance.claims.entity.ClaimService saved = claimServiceRepository.save(serviceLine);
        claimRepository.save(claim);

        log.info("Service line added with ID: {}", saved.getClaimServiceId());
        return claimMapper.toResponse(saved);
    }

    @Override
    public ClaimDiagnosisResponse addDiagnosis(UUID claimId, ClaimDiagnosisRequest request) {
        log.info("Adding diagnosis to claimId: {}", claimId);
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ClaimNotFoundException("Claim not found with ID: " + claimId));

        if (claim.getStatus() == ClaimStatus.SETTLED || claim.getStatus() == ClaimStatus.REJECTED) {
            throw new InvalidClaimStateException("Cannot add diagnosis to claim in state: " + claim.getStatus());
        }

        ClaimDiagnosis diagnosis = claimMapper.toEntity(request);
        claim.addDiagnosis(diagnosis);

        ClaimDiagnosis saved = claimDiagnosisRepository.save(diagnosis);
        return claimMapper.toResponse(saved);
    }

    @Override
    public ClaimDocumentResponse addDocumentReference(UUID claimId, ClaimDocumentRequest request) {
        log.info("Adding document reference to claimId: {}, documentId: {}", claimId, request.getDocumentId());
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ClaimNotFoundException("Claim not found with ID: " + claimId));

        ClaimDocument document = claimMapper.toEntity(request);
        claim.addDocument(document);

        ClaimDocument saved = claimDocumentRepository.save(document);
        log.info("Document reference added with ID: {}", saved.getClaimDocumentId());
        return claimMapper.toResponse(saved);
    }

    @Override
    public void validateStateTransition(ClaimStatus currentStatus, ClaimStatus targetStatus) {
        Set<ClaimStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Collections.emptySet());
        if (!allowed.contains(targetStatus)) {
            throw new InvalidClaimStateException(
                    String.format("Invalid state transition from %s to %s", currentStatus, targetStatus));
        }
    }

    private String generateClaimNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "CLM-" + datePart + "-" + randomPart;
    }
}
