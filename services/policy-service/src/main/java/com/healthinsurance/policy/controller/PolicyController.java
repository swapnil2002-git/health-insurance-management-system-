package com.healthinsurance.policy.controller;

import com.healthinsurance.policy.dto.request.PolicyCancellationRequest;
import com.healthinsurance.policy.dto.request.PolicyCreateRequest;
import com.healthinsurance.policy.dto.request.PolicyEndorsementRequest;
import com.healthinsurance.policy.dto.response.PolicyResponse;
import com.healthinsurance.policy.service.PolicyService;
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
@RequestMapping("/api/policies")
@RequiredArgsConstructor
@Tag(name = "Policy Controller", description = "Endpoints for Policy Management")
public class PolicyController {

    private final PolicyService policyService;

    @PostMapping
    @PreAuthorize("hasAnyRole('POLICY_ADMINISTRATOR', 'UNDERWRITER', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Create a new DRAFT policy")
    public ResponseEntity<PolicyResponse> createPolicy(@Valid @RequestBody PolicyCreateRequest request) {
        return new ResponseEntity<>(policyService.createPolicy(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get policy by ID")
    public ResponseEntity<PolicyResponse> getPolicy(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(policyService.getPolicy(id));
    }

    @GetMapping
    @Operation(summary = "Get all policies, optionally filtered by customer ID")
    public ResponseEntity<List<PolicyResponse>> getAllPolicies(
            @RequestParam(name = "customerId", required = false) UUID customerId) {
        if (customerId != null) {
            return ResponseEntity.ok(policyService.getPoliciesByCustomerId(customerId));
        }
        return ResponseEntity.ok(policyService.getAllPolicies());
    }

    @PostMapping("/{id}/issue")
    @PreAuthorize("hasAnyRole('POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Issue a policy (Transitions DRAFT to PENDING_PAYMENT)")
    public ResponseEntity<PolicyResponse> issuePolicy(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(policyService.issuePolicy(id));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Activate a policy (Transitions PENDING_PAYMENT to ACTIVE)")
    public ResponseEntity<PolicyResponse> activatePolicy(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(policyService.activatePolicy(id));
    }

    @PostMapping("/{id}/endorsements")
    @PreAuthorize("hasAnyRole('POLICY_ADMINISTRATOR', 'AGENT', 'CUSTOMER', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Add an endorsement to an ACTIVE policy")
    public ResponseEntity<PolicyResponse> addEndorsement(
            @PathVariable("id") UUID id,
            @Valid @RequestBody PolicyEndorsementRequest request) {
        return ResponseEntity.ok(policyService.addEndorsement(id, request));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('POLICY_ADMINISTRATOR', 'AGENT', 'CUSTOMER', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Cancel a policy")
    public ResponseEntity<PolicyResponse> cancelPolicy(
            @PathVariable("id") UUID id,
            @Valid @RequestBody PolicyCancellationRequest request) {
        return ResponseEntity.ok(policyService.cancelPolicy(id, request));
    }

    @PostMapping("/{id}/renew")
    @PreAuthorize("hasAnyRole('POLICY_ADMINISTRATOR', 'AGENT', 'CUSTOMER', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Renew an ACTIVE or EXPIRED policy")
    public ResponseEntity<PolicyResponse> renewPolicy(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(policyService.renewPolicy(id));
    }
}