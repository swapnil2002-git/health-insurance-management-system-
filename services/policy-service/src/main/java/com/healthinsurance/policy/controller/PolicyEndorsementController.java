package com.healthinsurance.policy.controller;

import com.healthinsurance.policy.dto.request.EndorsementApprovalRequest;
import com.healthinsurance.policy.dto.request.EndorsementRejectRequest;
import com.healthinsurance.policy.dto.request.PolicyEndorsementRequest;
import com.healthinsurance.policy.dto.response.PolicyEndorsementResponse;
import com.healthinsurance.policy.service.EndorsementService;
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
@RequestMapping("/api/policies/{policyId}/endorsements")
@RequiredArgsConstructor
@Tag(name = "Policy Endorsement Controller", description = "Endpoints for managing Policy Endorsements lifecycle")
public class PolicyEndorsementController {

    private final EndorsementService endorsementService;

    @PostMapping
    @PreAuthorize("hasAnyRole('POLICY_ADMINISTRATOR', 'AGENT', 'CUSTOMER', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Request an endorsement for an ACTIVE policy")
    public ResponseEntity<PolicyEndorsementResponse> requestEndorsement(
            @PathVariable("policyId") UUID policyId,
            @Valid @RequestBody PolicyEndorsementRequest request) {
        return new ResponseEntity<>(endorsementService.requestEndorsement(policyId, request), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all endorsements for a policy")
    public ResponseEntity<List<PolicyEndorsementResponse>> getEndorsementsByPolicy(
            @PathVariable("policyId") UUID policyId) {
        return ResponseEntity.ok(endorsementService.getEndorsementsByPolicy(policyId));
    }

    @GetMapping("/{endorsementId}")
    @Operation(summary = "Get endorsement details by ID")
    public ResponseEntity<PolicyEndorsementResponse> getEndorsement(
            @PathVariable("policyId") UUID policyId,
            @PathVariable("endorsementId") UUID endorsementId) {
        return ResponseEntity.ok(endorsementService.getEndorsement(policyId, endorsementId));
    }

    @PostMapping("/{endorsementId}/approve")
    @PreAuthorize("hasAnyRole('POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Approve and apply an endorsement to the policy")
    public ResponseEntity<PolicyEndorsementResponse> approveEndorsement(
            @PathVariable("policyId") UUID policyId,
            @PathVariable("endorsementId") UUID endorsementId,
            @Valid @RequestBody EndorsementApprovalRequest request) {
        return ResponseEntity.ok(endorsementService.approveEndorsement(policyId, endorsementId, request));
    }

    @PostMapping("/{endorsementId}/reject")
    @PreAuthorize("hasAnyRole('POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Reject an endorsement")
    public ResponseEntity<PolicyEndorsementResponse> rejectEndorsement(
            @PathVariable("policyId") UUID policyId,
            @PathVariable("endorsementId") UUID endorsementId,
            @Valid @RequestBody EndorsementRejectRequest request) {
        return ResponseEntity.ok(endorsementService.rejectEndorsement(policyId, endorsementId, request));
    }
}
