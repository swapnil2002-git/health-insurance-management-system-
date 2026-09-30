package com.healthinsurance.claims.service.impl;

import com.healthinsurance.claims.client.PolicyServiceClient;
import com.healthinsurance.claims.client.ProviderServiceClient;
import com.healthinsurance.claims.client.dto.PolicyClientDto;
import com.healthinsurance.claims.client.dto.ProviderClientDto;
import com.healthinsurance.claims.domain.ClaimStatus;
import com.healthinsurance.claims.domain.ValidationStatus;
import com.healthinsurance.claims.dto.ClaimValidationResponse;
import com.healthinsurance.claims.entity.Claim;
import com.healthinsurance.claims.entity.ClaimValidation;
import com.healthinsurance.claims.exception.ClaimNotFoundException;
import com.healthinsurance.claims.exception.ClaimValidationException;
import com.healthinsurance.claims.mapper.ClaimMapper;
import com.healthinsurance.claims.repository.ClaimRepository;
import com.healthinsurance.claims.repository.ClaimValidationRepository;
import com.healthinsurance.claims.service.ClaimService;
import com.healthinsurance.claims.service.ExternalVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(noRollbackFor = ClaimValidationException.class)
public class ExternalVerificationServiceImpl implements ExternalVerificationService {

    private final ClaimRepository claimRepository;
    private final ClaimValidationRepository claimValidationRepository;
    private final PolicyServiceClient policyServiceClient;
    private final ProviderServiceClient providerServiceClient;
    private final ClaimService claimService;
    private final ClaimMapper claimMapper;

    @Override
    public PolicyClientDto verifyPolicyAndMember(UUID claimId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ClaimNotFoundException("Claim not found with ID: " + claimId));
        log.info("Calling Policy Service for policyId: {}", claim.getPolicyId());
        return policyServiceClient.getPolicyById(claim.getPolicyId());
    }

    @Override
    public ProviderClientDto verifyProvider(UUID claimId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ClaimNotFoundException("Claim not found with ID: " + claimId));
        log.info("Calling Provider Service for providerId: {}", claim.getProviderId());
        return providerServiceClient.getProviderById(claim.getProviderId());
    }

    @Override
    public List<ClaimValidationResponse> performExternalEligibilityChecks(UUID claimId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ClaimNotFoundException("Claim not found with ID: " + claimId));

        log.info("Beginning external eligibility & provider check for claimId: {}", claimId);
        claimService.validateStateTransition(claim.getStatus(), ClaimStatus.ELIGIBILITY_CHECK);
        claim.setStatus(ClaimStatus.ELIGIBILITY_CHECK);

        List<ClaimValidation> records = new ArrayList<>();
        List<String> failures = new ArrayList<>();

        // Check 1: Policy Verification via Feign
        try {
            PolicyClientDto policy = verifyPolicyAndMember(claimId);
            if (!"ACTIVE".equalsIgnoreCase(policy.getStatus())) {
                String msg = "Policy is not active: status is " + policy.getStatus();
                records.add(recordValidation(claim, "POLICY_ACTIVE_CHECK", ValidationStatus.FAILED, msg));
                failures.add(msg);
            } else {
                records.add(recordValidation(claim, "POLICY_ACTIVE_CHECK", ValidationStatus.PASSED, "Policy is ACTIVE"));
            }

            // Check Coverage Dates
            LocalDate effectiveDate = policy.getEffectiveDate() != null 
                    ? policy.getEffectiveDate().atZone(ZoneOffset.UTC).toLocalDate() 
                    : null;
            LocalDate expiryDate = policy.getExpiryDate() != null 
                    ? policy.getExpiryDate().atZone(ZoneOffset.UTC).toLocalDate() 
                    : null;

            if (effectiveDate != null && claim.getServiceDate().isBefore(effectiveDate)) {
                String msg = "Service date is before policy effective date";
                records.add(recordValidation(claim, "POLICY_DATES_CHECK", ValidationStatus.FAILED, msg));
                failures.add(msg);
            } else if (expiryDate != null && claim.getServiceDate().isAfter(expiryDate)) {
                String msg = "Service date is after policy expiry date";
                records.add(recordValidation(claim, "POLICY_DATES_CHECK", ValidationStatus.FAILED, msg));
                failures.add(msg);
            } else {
                records.add(recordValidation(claim, "POLICY_DATES_CHECK", ValidationStatus.PASSED, "Service date is within policy coverage dates"));
            }

            // Check Member Enrollment
            boolean memberFound = (policy.getMembers() != null && policy.getMembers().stream()
                    .anyMatch(m -> claim.getMemberId().equals(m.getMemberId())))
                    || (policy.getCustomerId() != null && claim.getMemberId().equals(policy.getCustomerId()));
            if (!memberFound) {
                String msg = "Member " + claim.getMemberId() + " is not enrolled under policy " + claim.getPolicyId();
                records.add(recordValidation(claim, "MEMBER_ENROLLMENT_CHECK", ValidationStatus.FAILED, msg));
                failures.add(msg);
            } else {
                records.add(recordValidation(claim, "MEMBER_ENROLLMENT_CHECK", ValidationStatus.PASSED, "Member enrollment verified"));
            }

        } catch (Exception ex) {
            String msg = "Failed to verify policy with Policy Service: " + ex.getMessage();
            records.add(recordValidation(claim, "POLICY_SERVICE_CALL", ValidationStatus.FAILED, msg));
            failures.add(msg);
        }

        // Check 2: Provider Verification via Feign
        claimService.validateStateTransition(claim.getStatus(), ClaimStatus.PROVIDER_CHECK);
        claim.setStatus(ClaimStatus.PROVIDER_CHECK);

        try {
            ProviderClientDto provider = verifyProvider(claimId);
            if (!"ACTIVE".equalsIgnoreCase(provider.getStatus())) {
                String msg = "Provider is not active: status is " + provider.getStatus();
                records.add(recordValidation(claim, "PROVIDER_ACTIVE_CHECK", ValidationStatus.FAILED, msg));
                failures.add(msg);
            } else {
                records.add(recordValidation(claim, "PROVIDER_ACTIVE_CHECK", ValidationStatus.PASSED, "Provider is ACTIVE"));
            }
        } catch (Exception ex) {
            String msg = "Failed to verify provider with Provider Service: " + ex.getMessage();
            records.add(recordValidation(claim, "PROVIDER_SERVICE_CALL", ValidationStatus.FAILED, msg));
            failures.add(msg);
        }

        claimValidationRepository.saveAll(records);

        if (!failures.isEmpty()) {
            claim.setStatus(ClaimStatus.REJECTED);
            claimRepository.save(claim);
            log.warn("External verification for claim {} failed with {} errors: {}", claimId, failures.size(), failures);
            throw new ClaimValidationException("External verification failed: " + String.join("; ", failures), failures);
        }

        claim.setStatus(ClaimStatus.ADJUDICATION);
        claimRepository.save(claim);
        log.info("Claim {} passed external eligibility and provider verification", claimId);

        return records.stream().map(claimMapper::toResponse).collect(Collectors.toList());
    }

    private ClaimValidation recordValidation(Claim claim, String ruleName, ValidationStatus status, String message) {
        ClaimValidation validation = new ClaimValidation();
        validation.setClaim(claim);
        validation.setRuleName(ruleName);
        validation.setStatus(status);
        validation.setMessage(message);
        return validation;
    }
}
