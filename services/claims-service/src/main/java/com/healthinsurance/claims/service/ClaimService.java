package com.healthinsurance.claims.service;

import com.healthinsurance.claims.domain.ClaimStatus;
import com.healthinsurance.claims.dto.*;

import java.util.List;
import java.util.UUID;

public interface ClaimService {

    ClaimResponse createClaim(ClaimCreateRequest request);

    ClaimResponse createClaim(ClaimCreateRequest request, String idempotencyKey);

    ClaimResponse getClaimById(UUID claimId);

    ClaimResponse getClaimByNumber(String claimNumber);

    List<ClaimResponse> getAllClaims(ClaimStatus status);

    List<ClaimResponse> getClaimsByPolicyId(UUID policyId);

    List<ClaimResponse> getClaimsByMemberId(UUID memberId);

    ClaimServiceResponse addServiceLine(UUID claimId, ClaimServiceRequest request);

    ClaimDiagnosisResponse addDiagnosis(UUID claimId, ClaimDiagnosisRequest request);

    ClaimDocumentResponse addDocumentReference(UUID claimId, ClaimDocumentRequest request);

    void validateStateTransition(ClaimStatus currentStatus, ClaimStatus targetStatus);
}
