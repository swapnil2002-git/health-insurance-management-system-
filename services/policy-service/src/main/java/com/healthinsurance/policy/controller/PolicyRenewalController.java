package com.healthinsurance.policy.controller;

import com.healthinsurance.policy.dto.request.PolicyRenewalQuoteRequest;
import com.healthinsurance.policy.dto.request.RenewalPaymentRequest;
import com.healthinsurance.policy.dto.request.RenewalRejectRequest;
import com.healthinsurance.policy.dto.response.PolicyRenewalResponse;
import com.healthinsurance.policy.dto.response.RenewalEligibilityResponse;
import com.healthinsurance.policy.service.RenewalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/policies/{policyId}/renewals")
@RequiredArgsConstructor
@Tag(name = "Policy Renewal Controller", description = "Endpoints for managing Policy Renewal lifecycle, eligibility, quote generation, and term extension")
public class PolicyRenewalController {

    private final RenewalService renewalService;

    @GetMapping("/eligibility")
    @Operation(summary = "Check policy renewal eligibility")
    public ResponseEntity<RenewalEligibilityResponse> checkEligibility(
            @PathVariable("policyId") UUID policyId) {
        return ResponseEntity.ok(renewalService.checkEligibility(policyId));
    }

    @PostMapping("/quote")
    @PreAuthorize("hasAnyRole('POLICY_ADMINISTRATOR', 'AGENT', 'CUSTOMER', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Generate a renewal quote for an eligible policy")
    public ResponseEntity<PolicyRenewalResponse> generateRenewalQuote(
            @PathVariable("policyId") UUID policyId,
            @RequestBody(required = false) PolicyRenewalQuoteRequest request) {
        return new ResponseEntity<>(renewalService.generateRenewalQuote(policyId, request), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all renewals for a policy")
    public ResponseEntity<List<PolicyRenewalResponse>> getRenewalsByPolicy(
            @PathVariable("policyId") UUID policyId) {
        return ResponseEntity.ok(renewalService.getRenewalsByPolicy(policyId));
    }

    @GetMapping("/{renewalId}")
    @Operation(summary = "Get renewal details by ID")
    public ResponseEntity<PolicyRenewalResponse> getRenewal(
            @PathVariable("policyId") UUID policyId,
            @PathVariable("renewalId") UUID renewalId) {
        return ResponseEntity.ok(renewalService.getRenewal(policyId, renewalId));
    }

    @PostMapping("/{renewalId}/accept")
    @PreAuthorize("hasAnyRole('POLICY_ADMINISTRATOR', 'AGENT', 'CUSTOMER', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Accept renewal quote and transition to payment pending")
    public ResponseEntity<PolicyRenewalResponse> acceptRenewal(
            @PathVariable("policyId") UUID policyId,
            @PathVariable("renewalId") UUID renewalId) {
        return ResponseEntity.ok(renewalService.acceptRenewal(policyId, renewalId));
    }

    @PostMapping("/{renewalId}/complete")
    @PreAuthorize("hasAnyRole('POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Complete renewal with verified payment and extend policy term")
    public ResponseEntity<PolicyRenewalResponse> completeRenewal(
            @PathVariable("policyId") UUID policyId,
            @PathVariable("renewalId") UUID renewalId,
            @Valid @RequestBody RenewalPaymentRequest request) {
        return ResponseEntity.ok(renewalService.completeRenewal(policyId, renewalId, request));
    }

    @PostMapping("/{renewalId}/reject")
    @PreAuthorize("hasAnyRole('POLICY_ADMINISTRATOR', 'AGENT', 'CUSTOMER', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Reject renewal quote")
    public ResponseEntity<PolicyRenewalResponse> rejectRenewal(
            @PathVariable("policyId") UUID policyId,
            @PathVariable("renewalId") UUID renewalId,
            @Valid @RequestBody RenewalRejectRequest request) {
        return ResponseEntity.ok(renewalService.rejectRenewal(policyId, renewalId, request));
    }
}
