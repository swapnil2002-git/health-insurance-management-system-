package com.healthinsurance.claims.service;

import com.healthinsurance.claims.dto.ClaimPaymentResponse;
import com.healthinsurance.claims.dto.ClaimSettlementRequest;
import com.healthinsurance.claims.dto.ExplanationOfBenefitsResponse;

import java.util.List;
import java.util.UUID;

public interface ClaimSettlementService {

    ClaimPaymentResponse settleClaim(UUID claimId, ClaimSettlementRequest request);

    ExplanationOfBenefitsResponse getEobByClaimId(UUID claimId);

    ExplanationOfBenefitsResponse getEobByNumber(String eobNumber);

    List<ClaimPaymentResponse> getPaymentsByClaimId(UUID claimId);
}
