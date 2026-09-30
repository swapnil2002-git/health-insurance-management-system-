package com.healthinsurance.risk.controller;

import com.healthinsurance.risk.dto.request.CreateRiskAssessmentRequest;
import com.healthinsurance.risk.dto.response.RiskAssessmentResponse;
import com.healthinsurance.risk.service.RiskAssessmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/risk-assessments")
@RequiredArgsConstructor
public class RiskAssessmentController {

    private final RiskAssessmentService assessmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('UNDERWRITER', 'AGENT', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    public ResponseEntity<RiskAssessmentResponse> createAssessment(@Valid @RequestBody CreateRiskAssessmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assessmentService.createAssessment(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RiskAssessmentResponse> getAssessment(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(assessmentService.getAssessment(id));
    }

    @PostMapping("/{id}/calculate")
    @PreAuthorize("hasAnyRole('UNDERWRITER', 'AGENT', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    public ResponseEntity<RiskAssessmentResponse> calculateRisk(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(assessmentService.calculateRisk(id));
    }
}