package com.healthinsurance.claims.service;

import com.healthinsurance.claims.dto.ClaimAdjudicationResponse;

import java.util.UUID;

public interface ClaimAdjudicationService {

    ClaimAdjudicationResponse adjudicateClaim(UUID claimId);

    ClaimAdjudicationResponse getAdjudicationByClaimId(UUID claimId);
}
