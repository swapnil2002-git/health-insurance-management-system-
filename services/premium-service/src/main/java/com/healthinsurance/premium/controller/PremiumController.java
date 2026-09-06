package com.healthinsurance.premium.controller;

import com.healthinsurance.premium.dto.request.InstallmentPaymentRequest;
import com.healthinsurance.premium.dto.request.PremiumRecalculateRequest;
import com.healthinsurance.premium.dto.request.PremiumScheduleCreateRequest;
import com.healthinsurance.premium.dto.response.PremiumInstallmentResponse;
import com.healthinsurance.premium.dto.response.PremiumOutstandingResponse;
import com.healthinsurance.premium.dto.response.PremiumScheduleResponse;
import com.healthinsurance.premium.service.PremiumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Premium Controller", description = "Endpoints for Premium Schedules, Installments, and Calculations")
public class PremiumController {

    private final PremiumService premiumService;

    @PostMapping("/api/premium-schedules")
    @Operation(summary = "Create a premium schedule manually")
    public ResponseEntity<PremiumScheduleResponse> createPremiumSchedule(
            @Valid @RequestBody PremiumScheduleCreateRequest request) {
        return new ResponseEntity<>(premiumService.createPremiumSchedule(request), HttpStatus.CREATED);
    }

    @GetMapping("/api/policies/{policyId}/premium")
    @Operation(summary = "Get the premium schedule and installments for a policy")
    public ResponseEntity<PremiumScheduleResponse> getPremiumByPolicy(
            @PathVariable("policyId") UUID policyId) {
        return ResponseEntity.ok(premiumService.getPremiumByPolicy(policyId));
    }

    @GetMapping("/api/policies/{policyId}/premium/outstanding")
    @Operation(summary = "Get current outstanding premium amount for a policy")
    public ResponseEntity<PremiumOutstandingResponse> getOutstanding(
            @PathVariable("policyId") UUID policyId) {
        return ResponseEntity.ok(premiumService.getOutstanding(policyId));
    }

    @PostMapping("/api/policies/{policyId}/premium/recalculate")
    @Operation(summary = "Recalculate future installments of a premium schedule")
    public ResponseEntity<PremiumScheduleResponse> recalculatePremium(
            @PathVariable("policyId") UUID policyId,
            @RequestBody PremiumRecalculateRequest request) {
        return ResponseEntity.ok(premiumService.recalculatePremium(policyId, request));
    }

    @PostMapping("/api/premium-schedules/installments/{installmentId}/pay")
    @Operation(summary = "Record payment for a premium installment")
    public ResponseEntity<PremiumInstallmentResponse> recordInstallmentPayment(
            @PathVariable("installmentId") UUID installmentId,
            @Valid @RequestBody InstallmentPaymentRequest request) {
        return ResponseEntity.ok(premiumService.recordInstallmentPayment(installmentId, request));
    }
}