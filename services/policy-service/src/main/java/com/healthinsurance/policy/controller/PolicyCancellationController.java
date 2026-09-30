package com.healthinsurance.policy.controller;

import com.healthinsurance.policy.dto.request.CancellationApprovalRequest;
import com.healthinsurance.policy.dto.request.CancellationRejectRequest;
import com.healthinsurance.policy.dto.request.PolicyCancellationRequest;
import com.healthinsurance.policy.dto.response.PolicyCancellationResponse;
import com.healthinsurance.policy.service.CancellationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/policies/{policyId}/cancellation")
@RequiredArgsConstructor
@Tag(name = "Policy Cancellation Controller", description = "Endpoints for managing Policy Cancellation lifecycle and refunds")
public class PolicyCancellationController {

    private final CancellationService cancellationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('POLICY_ADMINISTRATOR', 'AGENT', 'CUSTOMER', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Request policy cancellation")
    public ResponseEntity<PolicyCancellationResponse> requestCancellation(
            @PathVariable("policyId") UUID policyId,
            @Valid @RequestBody PolicyCancellationRequest request) {
        return new ResponseEntity<>(cancellationService.requestCancellation(policyId, request), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get cancellation details for a policy")
    public ResponseEntity<PolicyCancellationResponse> getCancellation(
            @PathVariable("policyId") UUID policyId) {
        return ResponseEntity.ok(cancellationService.getCancellation(policyId));
    }

    @PostMapping("/approve")
    @PreAuthorize("hasAnyRole('POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Approve policy cancellation and execute refund")
    public ResponseEntity<PolicyCancellationResponse> approveCancellation(
            @PathVariable("policyId") UUID policyId,
            @Valid @RequestBody CancellationApprovalRequest request) {
        return ResponseEntity.ok(cancellationService.approveCancellation(policyId, request));
    }

    @PostMapping("/reject")
    @PreAuthorize("hasAnyRole('POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Reject policy cancellation")
    public ResponseEntity<PolicyCancellationResponse> rejectCancellation(
            @PathVariable("policyId") UUID policyId,
            @Valid @RequestBody CancellationRejectRequest request) {
        return ResponseEntity.ok(cancellationService.rejectCancellation(policyId, request));
    }
}
