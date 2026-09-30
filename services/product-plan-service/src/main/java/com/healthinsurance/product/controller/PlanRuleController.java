package com.healthinsurance.product.controller;

import com.healthinsurance.product.dto.request.PlanRuleRequest;
import com.healthinsurance.product.dto.response.RuleResponse;
import com.healthinsurance.product.service.PlanRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/plans/{planId}")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
public class PlanRuleController {

    private final PlanRuleService planRuleService;

    @PostMapping("/coverages")
    public ResponseEntity<RuleResponse> addCoverage(
            @PathVariable("planId") UUID planId,
            @Valid @RequestBody PlanRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planRuleService.addCoverage(planId, request));
    }

    @PostMapping("/exclusions")
    public ResponseEntity<RuleResponse> addExclusion(
            @PathVariable("planId") UUID planId,
            @Valid @RequestBody PlanRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planRuleService.addExclusion(planId, request));
    }

    @PostMapping("/deductibles")
    public ResponseEntity<RuleResponse> addDeductible(
            @PathVariable("planId") UUID planId,
            @Valid @RequestBody PlanRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planRuleService.addDeductible(planId, request));
    }

    @PostMapping("/copayments")
    public ResponseEntity<RuleResponse> addCopayment(
            @PathVariable("planId") UUID planId,
            @Valid @RequestBody PlanRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planRuleService.addCopayment(planId, request));
    }

    @PostMapping("/riders")
    public ResponseEntity<RuleResponse> addRider(
            @PathVariable("planId") UUID planId,
            @Valid @RequestBody PlanRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planRuleService.addRider(planId, request));
    }
}