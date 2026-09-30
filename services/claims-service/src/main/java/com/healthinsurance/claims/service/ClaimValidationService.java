package com.healthinsurance.claims.service;

import com.healthinsurance.claims.dto.ClaimValidationResponse;
import com.healthinsurance.claims.entity.Claim;

import java.util.List;
import java.util.UUID;

public interface ClaimValidationService {

    List<ClaimValidationResponse> validateClaim(UUID claimId);

    void checkForDuplicates(Claim claim);
}
