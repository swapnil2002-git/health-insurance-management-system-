package com.healthinsurance.claims.service.impl;

import com.healthinsurance.claims.domain.ClaimStatus;
import com.healthinsurance.claims.domain.ValidationStatus;
import com.healthinsurance.claims.dto.ClaimValidationResponse;
import com.healthinsurance.claims.entity.Claim;
import com.healthinsurance.claims.entity.ClaimValidation;
import com.healthinsurance.claims.event.ClaimEventProducer;
import com.healthinsurance.claims.exception.ClaimNotFoundException;
import com.healthinsurance.claims.exception.ClaimValidationException;
import com.healthinsurance.claims.exception.DuplicateClaimException;
import com.healthinsurance.claims.mapper.ClaimMapper;
import com.healthinsurance.claims.repository.ClaimRepository;
import com.healthinsurance.claims.repository.ClaimValidationRepository;
import com.healthinsurance.claims.service.ClaimService;
import com.healthinsurance.claims.service.ClaimValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(noRollbackFor = ClaimValidationException.class)
public class ClaimValidationServiceImpl implements ClaimValidationService {

    private final ClaimRepository claimRepository;
    private final ClaimValidationRepository claimValidationRepository;
    private final ClaimService claimService;
    private final ClaimMapper claimMapper;
    private final ClaimEventProducer claimEventProducer;

    @Override
    public List<ClaimValidationResponse> validateClaim(UUID claimId) {
        log.info("Performing SDD validation for claimId: {}", claimId);
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ClaimNotFoundException("Claim not found with ID: " + claimId));

        claimService.validateStateTransition(claim.getStatus(), ClaimStatus.VALIDATING);
        claim.setStatus(ClaimStatus.VALIDATING);

        List<ClaimValidation> validationRecords = new ArrayList<>();
        List<String> failureReasons = new ArrayList<>();

        // Rule 1: Duplicate Check
        try {
            checkForDuplicates(claim);
            validationRecords.add(recordValidation(claim, "DUPLICATE_CHECK", ValidationStatus.PASSED, "No duplicate claim found for member and service date"));
        } catch (DuplicateClaimException ex) {
            validationRecords.add(recordValidation(claim, "DUPLICATE_CHECK", ValidationStatus.FAILED, ex.getMessage()));
            failureReasons.add(ex.getMessage());
        }

        // Rule 2: Service Date in Past / Today
        if (claim.getServiceDate() != null && claim.getServiceDate().isAfter(LocalDate.now())) {
            String msg = "Service date cannot be in the future: " + claim.getServiceDate();
            validationRecords.add(recordValidation(claim, "SERVICE_DATE_VALIDITY", ValidationStatus.FAILED, msg));
            failureReasons.add(msg);
        } else {
            validationRecords.add(recordValidation(claim, "SERVICE_DATE_VALIDITY", ValidationStatus.PASSED, "Service date is valid"));
        }

        // Rule 3: Must have at least one billable service line
        if (claim.getServiceLines() == null || claim.getServiceLines().isEmpty()) {
            String msg = "Claim must contain at least one billable service line";
            validationRecords.add(recordValidation(claim, "SERVICE_LINES_REQUIRED", ValidationStatus.FAILED, msg));
            failureReasons.add(msg);
        } else {
            validationRecords.add(recordValidation(claim, "SERVICE_LINES_REQUIRED", ValidationStatus.PASSED, "Billable service lines verified (" + claim.getServiceLines().size() + " lines)"));
        }

        // Rule 4: Must have at least one diagnosis code
        if (claim.getDiagnoses() == null || claim.getDiagnoses().isEmpty()) {
            String msg = "Claim must contain at least one diagnosis code";
            validationRecords.add(recordValidation(claim, "DIAGNOSES_REQUIRED", ValidationStatus.FAILED, msg));
            failureReasons.add(msg);
        } else {
            validationRecords.add(recordValidation(claim, "DIAGNOSES_REQUIRED", ValidationStatus.PASSED, "Diagnosis codes verified (" + claim.getDiagnoses().size() + " codes)"));
        }

        // Rule 5: Non-zero total claim amount
        if (claim.getTotalClaimAmount() == null || claim.getTotalClaimAmount().signum() <= 0) {
            String msg = "Total claim amount must be greater than zero";
            validationRecords.add(recordValidation(claim, "TOTAL_AMOUNT_VALIDITY", ValidationStatus.FAILED, msg));
            failureReasons.add(msg);
        } else {
            validationRecords.add(recordValidation(claim, "TOTAL_AMOUNT_VALIDITY", ValidationStatus.PASSED, "Total claim amount is positive: " + claim.getTotalClaimAmount()));
        }

        claimValidationRepository.saveAll(validationRecords);

        if (!failureReasons.isEmpty()) {
            claim.setStatus(ClaimStatus.REJECTED);
            Claim rejected = claimRepository.save(claim);
            log.warn("Claim {} validation failed with {} errors: {}", claimId, failureReasons.size(), failureReasons);
            claimEventProducer.publishClaimRejected(rejected, String.join("; ", failureReasons));
            throw new ClaimValidationException("Claim validation failed: " + String.join("; ", failureReasons), failureReasons);
        }

        Claim saved = claimRepository.save(claim);
        log.info("Claim {} passed all initial validation rules successfully", claimId);
        claimEventProducer.publishClaimValidated(saved);
        return validationRecords.stream().map(claimMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public void checkForDuplicates(Claim claim) {
        boolean duplicateExists = claimRepository.existsByPolicyIdAndMemberIdAndServiceDateAndStatusNot(
                claim.getPolicyId(),
                claim.getMemberId(),
                claim.getServiceDate(),
                ClaimStatus.REJECTED
        );

        if (duplicateExists) {
            List<Claim> existingClaims = claimRepository.findByMemberId(claim.getMemberId());
            boolean match = existingClaims.stream().anyMatch(c ->
                    !c.getClaimId().equals(claim.getClaimId())
                    && c.getPolicyId().equals(claim.getPolicyId())
                    && c.getServiceDate().equals(claim.getServiceDate())
                    && c.getStatus() != ClaimStatus.REJECTED);

            if (match) {
                throw new DuplicateClaimException(String.format(
                        "Duplicate claim detected for member %s, policy %s on service date %s",
                        claim.getMemberId(), claim.getPolicyId(), claim.getServiceDate()
                ));
            }
        }
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
