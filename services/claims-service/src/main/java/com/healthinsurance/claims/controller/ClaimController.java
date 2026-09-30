package com.healthinsurance.claims.controller;

import com.healthinsurance.claims.domain.ClaimStatus;
import com.healthinsurance.claims.dto.*;
import com.healthinsurance.claims.service.ClaimAdjudicationService;
import com.healthinsurance.claims.service.ClaimService;
import com.healthinsurance.claims.service.ClaimSettlementService;
import com.healthinsurance.claims.service.ClaimValidationService;
import com.healthinsurance.claims.service.ExternalVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
@Tag(name = "Claim Management", description = "Endpoints for managing healthcare claims throughout their lifecycle")
public class ClaimController {

    private final ClaimService claimService;
    private final ClaimValidationService claimValidationService;
    private final ExternalVerificationService externalVerificationService;
    private final ClaimAdjudicationService claimAdjudicationService;
    private final ClaimSettlementService claimSettlementService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'CLAIMS_OFFICER', 'SYSTEM_ADMINISTRATOR')")
    @Operation(summary = "Submit a new healthcare claim", description = "Creates a claim in SUBMITTED status and publishes CLAIM_SUBMITTED event. Supports Idempotency-Key header.")
    public ResponseEntity<ClaimResponse> createClaim(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody ClaimCreateRequest request) {
        log.info("REST request to create claim for policyId: {}, idempotencyKey: {}", request.getPolicyId(), idempotencyKey);
        ClaimResponse response = claimService.createClaim(request, idempotencyKey);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{claimId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'CLAIMS_OFFICER', 'UNDERWRITER', 'POLICY_ADMINISTRATOR', 'FINANCE_OFFICER', 'SYSTEM_ADMINISTRATOR')")
    @Operation(summary = "Get claim by ID", description = "Retrieves full claim details by claim UUID")
    public ResponseEntity<ClaimResponse> getClaimById(@PathVariable("claimId") UUID claimId) {
        log.info("REST request to get claim by ID: {}", claimId);
        ClaimResponse response = claimService.getClaimById(claimId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/number/{claimNumber}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'CLAIMS_OFFICER', 'UNDERWRITER', 'POLICY_ADMINISTRATOR', 'FINANCE_OFFICER', 'SYSTEM_ADMINISTRATOR')")
    @Operation(summary = "Get claim by claim number", description = "Retrieves claim by human-readable claim number")
    public ResponseEntity<ClaimResponse> getClaimByNumber(@PathVariable("claimNumber") String claimNumber) {
        log.info("REST request to get claim by number: {}", claimNumber);
        ClaimResponse response = claimService.getClaimByNumber(claimNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('AGENT', 'CLAIMS_OFFICER', 'UNDERWRITER', 'POLICY_ADMINISTRATOR', 'FINANCE_OFFICER', 'SYSTEM_ADMINISTRATOR')")
    @Operation(summary = "List all claims", description = "Retrieves claims optionally filtered by status")
    public ResponseEntity<List<ClaimResponse>> getAllClaims(@RequestParam(value = "status", required = false) ClaimStatus status) {
        log.info("REST request to list claims with status filter: {}", status);
        List<ClaimResponse> response = claimService.getAllClaims(status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/policy/{policyId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'CLAIMS_OFFICER', 'UNDERWRITER', 'POLICY_ADMINISTRATOR', 'FINANCE_OFFICER', 'SYSTEM_ADMINISTRATOR')")
    @Operation(summary = "Get claims by policy ID", description = "Retrieves all claims submitted against a specific policy")
    public ResponseEntity<List<ClaimResponse>> getClaimsByPolicyId(@PathVariable("policyId") UUID policyId) {
        log.info("REST request to get claims by policyId: {}", policyId);
        List<ClaimResponse> response = claimService.getClaimsByPolicyId(policyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'CLAIMS_OFFICER', 'UNDERWRITER', 'POLICY_ADMINISTRATOR', 'FINANCE_OFFICER', 'SYSTEM_ADMINISTRATOR')")
    @Operation(summary = "Get claims by member ID", description = "Retrieves all claims associated with an insured member")
    public ResponseEntity<List<ClaimResponse>> getClaimsByMemberId(@PathVariable("memberId") UUID memberId) {
        log.info("REST request to get claims by memberId: {}", memberId);
        List<ClaimResponse> response = claimService.getClaimsByMemberId(memberId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{claimId}/service-lines")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'CLAIMS_OFFICER', 'SYSTEM_ADMINISTRATOR')")
    @Operation(summary = "Add service line item", description = "Adds a billable procedure or service item to an active claim")
    public ResponseEntity<ClaimServiceResponse> addServiceLine(
            @PathVariable("claimId") UUID claimId,
            @Valid @RequestBody ClaimServiceRequest request) {
        log.info("REST request to add service line to claimId: {}", claimId);
        ClaimServiceResponse response = claimService.addServiceLine(claimId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{claimId}/diagnoses")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'CLAIMS_OFFICER', 'SYSTEM_ADMINISTRATOR')")
    @Operation(summary = "Add diagnosis code", description = "Adds an ICD-10 diagnosis code to an active claim")
    public ResponseEntity<ClaimDiagnosisResponse> addDiagnosis(
            @PathVariable("claimId") UUID claimId,
            @Valid @RequestBody ClaimDiagnosisRequest request) {
        log.info("REST request to add diagnosis to claimId: {}", claimId);
        ClaimDiagnosisResponse response = claimService.addDiagnosis(claimId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{claimId}/documents")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'CLAIMS_OFFICER', 'SYSTEM_ADMINISTRATOR')")
    @Operation(summary = "Link document reference", description = "Links a Document Service document reference UUID to a claim")
    public ResponseEntity<ClaimDocumentResponse> addDocumentReference(
            @PathVariable("claimId") UUID claimId,
            @Valid @RequestBody ClaimDocumentRequest request) {
        log.info("REST request to add document reference to claimId: {}", claimId);
        ClaimDocumentResponse response = claimService.addDocumentReference(claimId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{claimId}/validate")
    @PreAuthorize("hasAnyRole('CLAIMS_OFFICER', 'SYSTEM_ADMINISTRATOR')")
    @Operation(summary = "Validate claim rules", description = "Executes initial validation rules (duplicates, service lines, diagnosis, positive amounts)")
    public ResponseEntity<List<ClaimValidationResponse>> validateClaim(@PathVariable("claimId") UUID claimId) {
        log.info("REST request to validate claim: {}", claimId);
        List<ClaimValidationResponse> response = claimValidationService.validateClaim(claimId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{claimId}/verify-eligibility")
    @PreAuthorize("hasAnyRole('CLAIMS_OFFICER', 'SYSTEM_ADMINISTRATOR')")
    @Operation(summary = "Verify eligibility via Policy and Provider services", description = "Validates active policy, member coverage window, and active provider status via Feign")
    public ResponseEntity<List<ClaimValidationResponse>> verifyEligibility(@PathVariable("claimId") UUID claimId) {
        log.info("REST request to verify external eligibility for claim: {}", claimId);
        List<ClaimValidationResponse> response = externalVerificationService.performExternalEligibilityChecks(claimId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{claimId}/adjudicate")
    @PreAuthorize("hasAnyRole('CLAIMS_OFFICER', 'SYSTEM_ADMINISTRATOR')")
    @Operation(summary = "Adjudicate claim", description = "Calculates deductible, copay, customer responsibility, insurer payable, and final decision")
    public ResponseEntity<ClaimAdjudicationResponse> adjudicateClaim(@PathVariable("claimId") UUID claimId) {
        log.info("REST request to adjudicate claim: {}", claimId);
        ClaimAdjudicationResponse response = claimAdjudicationService.adjudicateClaim(claimId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{claimId}/adjudication")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'CLAIMS_OFFICER', 'FINANCE_OFFICER', 'SYSTEM_ADMINISTRATOR')")
    @Operation(summary = "Get claim adjudication details", description = "Retrieves the financial breakdown and adjudication decision for a claim")
    public ResponseEntity<ClaimAdjudicationResponse> getAdjudication(@PathVariable("claimId") UUID claimId) {
        log.info("REST request to get adjudication details for claim: {}", claimId);
        ClaimAdjudicationResponse response = claimAdjudicationService.getAdjudicationByClaimId(claimId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{claimId}/settle")
    @PreAuthorize("hasAnyRole('CLAIMS_OFFICER', 'FINANCE_OFFICER', 'SYSTEM_ADMINISTRATOR')")
    @Operation(summary = "Settle claim and issue payment", description = "Settles an approved claim, records payment details, and generates the Explanation of Benefits")
    public ResponseEntity<ClaimPaymentResponse> settleClaim(
            @PathVariable("claimId") UUID claimId,
            @Valid @RequestBody ClaimSettlementRequest request) {
        log.info("REST request to settle claim: {}", claimId);
        ClaimPaymentResponse response = claimSettlementService.settleClaim(claimId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{claimId}/eob")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'CLAIMS_OFFICER', 'FINANCE_OFFICER', 'SYSTEM_ADMINISTRATOR')")
    @Operation(summary = "Get Explanation of Benefits (EOB) by claim ID", description = "Retrieves the formal EOB statement generated for a settled claim")
    public ResponseEntity<ExplanationOfBenefitsResponse> getEobByClaimId(@PathVariable("claimId") UUID claimId) {
        log.info("REST request to get EOB for claim: {}", claimId);
        ExplanationOfBenefitsResponse response = claimSettlementService.getEobByClaimId(claimId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/eob/{eobNumber}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'CLAIMS_OFFICER', 'FINANCE_OFFICER', 'SYSTEM_ADMINISTRATOR')")
    @Operation(summary = "Get Explanation of Benefits (EOB) by EOB number", description = "Retrieves the formal EOB statement by unique EOB number")
    public ResponseEntity<ExplanationOfBenefitsResponse> getEobByNumber(@PathVariable("eobNumber") String eobNumber) {
        log.info("REST request to get EOB by eobNumber: {}", eobNumber);
        ExplanationOfBenefitsResponse response = claimSettlementService.getEobByNumber(eobNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{claimId}/payments")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'CLAIMS_OFFICER', 'FINANCE_OFFICER', 'SYSTEM_ADMINISTRATOR')")
    @Operation(summary = "Get payment history for claim", description = "Retrieves all disbursement records generated for a claim")
    public ResponseEntity<List<ClaimPaymentResponse>> getPaymentsByClaimId(@PathVariable("claimId") UUID claimId) {
        log.info("REST request to get payments for claim: {}", claimId);
        List<ClaimPaymentResponse> response = claimSettlementService.getPaymentsByClaimId(claimId);
        return ResponseEntity.ok(response);
    }
}
