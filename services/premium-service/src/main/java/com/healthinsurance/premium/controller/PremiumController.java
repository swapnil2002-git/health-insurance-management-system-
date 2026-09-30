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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Premium Controller", description = "Endpoints for Premium Schedules, Installments, and Calculations")
public class PremiumController {

    private final PremiumService premiumService;

    @GetMapping("/api/premium-schedules")
    @PreAuthorize("hasAnyRole('POLICY_ADMINISTRATOR', 'FINANCE_OFFICER', 'AGENT', 'CUSTOMER', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Get all premium schedules")
    public ResponseEntity<List<PremiumScheduleResponse>> getAllSchedules() {
        return ResponseEntity.ok(premiumService.getAllSchedules());
    }

    @PostMapping("/api/premium-schedules")
    @PreAuthorize("hasAnyRole('POLICY_ADMINISTRATOR', 'FINANCE_OFFICER', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Create a premium schedule manually")
    public ResponseEntity<PremiumScheduleResponse> createPremiumSchedule(
            @Valid @RequestBody PremiumScheduleCreateRequest request) {
        return new ResponseEntity<>(premiumService.createPremiumSchedule(request), HttpStatus.CREATED);
    }

    @GetMapping("/api/policies/{policyId}/premium")
    @PreAuthorize("hasAnyRole('POLICY_ADMINISTRATOR', 'FINANCE_OFFICER', 'AGENT', 'CUSTOMER', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Get the premium schedule and installments for a policy")
    public ResponseEntity<PremiumScheduleResponse> getPremiumByPolicy(
            @PathVariable("policyId") UUID policyId) {
        return ResponseEntity.ok(premiumService.getPremiumByPolicy(policyId));
    }

    @GetMapping("/api/premium-schedules/{scheduleId}")
    @PreAuthorize("hasAnyRole('POLICY_ADMINISTRATOR', 'FINANCE_OFFICER', 'AGENT', 'CUSTOMER', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Get premium schedule by schedule ID")
    public ResponseEntity<PremiumScheduleResponse> getScheduleById(
            @PathVariable("scheduleId") UUID scheduleId) {
        return ResponseEntity.ok(premiumService.getScheduleById(scheduleId));
    }

    @GetMapping("/api/policies/{policyId}/premium/outstanding")
    @PreAuthorize("hasAnyRole('POLICY_ADMINISTRATOR', 'FINANCE_OFFICER', 'AGENT', 'CUSTOMER', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Get current outstanding premium amount for a policy")
    public ResponseEntity<PremiumOutstandingResponse> getOutstanding(
            @PathVariable("policyId") UUID policyId) {
        return ResponseEntity.ok(premiumService.getOutstanding(policyId));
    }

    @PostMapping("/api/policies/{policyId}/premium/recalculate")
    @PreAuthorize("hasAnyRole('POLICY_ADMINISTRATOR', 'FINANCE_OFFICER', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Recalculate future installments of a premium schedule")
    public ResponseEntity<PremiumScheduleResponse> recalculatePremium(
            @PathVariable("policyId") UUID policyId,
            @Valid @RequestBody PremiumRecalculateRequest request) {
        return ResponseEntity.ok(premiumService.recalculatePremium(policyId, request));
    }

    @PostMapping("/api/premium-schedules/installments/{installmentId}/pay")
    @PreAuthorize("hasAnyRole('FINANCE_OFFICER', 'POLICY_ADMINISTRATOR', 'CUSTOMER', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Record payment for a premium installment")
    public ResponseEntity<PremiumInstallmentResponse> recordInstallmentPayment(
            @PathVariable("installmentId") UUID installmentId,
            @Valid @RequestBody InstallmentPaymentRequest request) {
        return ResponseEntity.ok(premiumService.recordInstallmentPayment(installmentId, request));
    }
}