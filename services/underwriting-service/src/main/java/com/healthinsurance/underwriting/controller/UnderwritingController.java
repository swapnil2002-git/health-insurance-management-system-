package com.healthinsurance.underwriting.controller;

import com.healthinsurance.underwriting.dto.request.*;
import com.healthinsurance.underwriting.dto.response.UnderwritingCaseResponse;
import com.healthinsurance.underwriting.service.UnderwritingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/underwriting/cases")
@RequiredArgsConstructor
public class UnderwritingController {

    private final UnderwritingService underwritingService;

    @PostMapping
    public ResponseEntity<UnderwritingCaseResponse> createCase(@Valid @RequestBody CreateUnderwritingCaseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(underwritingService.createCase(request));
    }

    @GetMapping("/{caseId}")
    public ResponseEntity<UnderwritingCaseResponse> getCase(@PathVariable("caseId") UUID caseId) {
        return ResponseEntity.ok(underwritingService.getCase(caseId));
    }

    @PostMapping("/{caseId}/approve")
    public ResponseEntity<UnderwritingCaseResponse> approveCase(
            @PathVariable("caseId") UUID caseId,
            @Valid @RequestBody ApproveUnderwritingRequest request) {
        return ResponseEntity.ok(underwritingService.approve(caseId, request));
    }

    @PostMapping("/{caseId}/reject")
    public ResponseEntity<UnderwritingCaseResponse> rejectCase(
            @PathVariable("caseId") UUID caseId,
            @Valid @RequestBody RejectUnderwritingRequest request) {
        return ResponseEntity.ok(underwritingService.reject(caseId, request));
    }

    @PostMapping("/{caseId}/refer")
    public ResponseEntity<UnderwritingCaseResponse> referCase(
            @PathVariable("caseId") UUID caseId,
            @Valid @RequestBody ReferUnderwritingRequest request) {
        return ResponseEntity.ok(underwritingService.refer(caseId, request));
    }
}