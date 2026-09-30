package com.healthinsurance.product.controller;

import com.healthinsurance.product.dto.request.PlanRequest;
import com.healthinsurance.product.dto.response.PlanDetailResponse;
import com.healthinsurance.product.dto.response.PlanResponse;
import com.healthinsurance.product.service.PlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @PostMapping
    @PreAuthorize("hasAnyRole('POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    public ResponseEntity<PlanResponse> createPlan(@Valid @RequestBody PlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planService.createPlan(request));
    }

    @GetMapping
    public ResponseEntity<List<PlanResponse>> getAllPlans() {
        return ResponseEntity.ok(planService.getAllPlans());
    }

    @GetMapping("/{planId}")
    public ResponseEntity<PlanDetailResponse> getPlan(@PathVariable("planId") UUID planId) {
        return ResponseEntity.ok(planService.getPlan(planId));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<PlanResponse>> getPlansByProduct(@PathVariable("productId") UUID productId) {
        return ResponseEntity.ok(planService.getPlansByProductId(productId));
    }

    @PutMapping("/{planId}")
    @PreAuthorize("hasAnyRole('POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    public ResponseEntity<PlanResponse> updatePlan(
            @PathVariable("planId") UUID planId,
            @Valid @RequestBody PlanRequest request) {
        return ResponseEntity.ok(planService.updatePlan(planId, request));
    }
}