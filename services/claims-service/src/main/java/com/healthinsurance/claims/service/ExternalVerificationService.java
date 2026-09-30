package com.healthinsurance.claims.service;

import com.healthinsurance.claims.client.dto.PolicyClientDto;
import com.healthinsurance.claims.client.dto.ProviderClientDto;
import com.healthinsurance.claims.dto.ClaimValidationResponse;

import java.util.List;
import java.util.UUID;

public interface ExternalVerificationService {

    PolicyClientDto verifyPolicyAndMember(UUID claimId);

    ProviderClientDto verifyProvider(UUID claimId);

    List<ClaimValidationResponse> performExternalEligibilityChecks(UUID claimId);
}
