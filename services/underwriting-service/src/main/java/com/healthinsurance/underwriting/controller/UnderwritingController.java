package com.healthinsurance.underwriting.controller;

import com.healthinsurance.underwriting.dto.request.*;
import com.healthinsurance.underwriting.dto.response.UnderwritingCaseResponse;
import com.healthinsurance.underwriting.service.UnderwritingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/underwriting/cases")
@RequiredArgsConstructor
public class UnderwritingController {

    private final UnderwritingService underwritingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('UNDERWRITER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    public ResponseEntity<List<UnderwritingCaseResponse>> getAllCases() {
        return ResponseEntity.ok(underwritingService.getAllCases());
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('UNDERWRITER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    public ResponseEntity<List<UnderwritingCaseResponse>> getCasesByCustomer(@PathVariable("customerId") UUID customerId) {
        return ResponseEntity.ok(underwritingService.getCasesByCustomerId(customerId));
    }

    @GetMapping("/quote/{quoteId}")
    @PreAuthorize("hasAnyRole('UNDERWRITER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    public ResponseEntity<List<UnderwritingCaseResponse>> getCasesByQuote(@PathVariable("quoteId") UUID quoteId) {
        return ResponseEntity.ok(underwritingService.getCasesByQuoteId(quoteId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('UNDERWRITER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    public ResponseEntity<UnderwritingCaseResponse> createCase(@Valid @RequestBody CreateUnderwritingCaseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(underwritingService.createCase(request));
    }

    @GetMapping("/{caseId}")
    public ResponseEntity<UnderwritingCaseResponse> getCase(@PathVariable("caseId") UUID caseId) {
        return ResponseEntity.ok(underwritingService.getCase(caseId));
    }

    @PostMapping("/{caseId}/approve")
    @PreAuthorize("hasAnyRole('UNDERWRITER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    public ResponseEntity<UnderwritingCaseResponse> approveCase(
            @PathVariable("caseId") UUID caseId,
            @Valid @RequestBody ApproveUnderwritingRequest request) {
        return ResponseEntity.ok(underwritingService.approve(caseId, request));
    }

    @PostMapping("/{caseId}/reject")
    @PreAuthorize("hasAnyRole('UNDERWRITER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    public ResponseEntity<UnderwritingCaseResponse> rejectCase(
            @PathVariable("caseId") UUID caseId,
            @Valid @RequestBody RejectUnderwritingRequest request) {
        return ResponseEntity.ok(underwritingService.reject(caseId, request));
    }

    @PostMapping("/{caseId}/refer")
    @PreAuthorize("hasAnyRole('UNDERWRITER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    public ResponseEntity<UnderwritingCaseResponse> referCase(
            @PathVariable("caseId") UUID caseId,
            @Valid @RequestBody ReferUnderwritingRequest request) {
        return ResponseEntity.ok(underwritingService.refer(caseId, request));
    }
}