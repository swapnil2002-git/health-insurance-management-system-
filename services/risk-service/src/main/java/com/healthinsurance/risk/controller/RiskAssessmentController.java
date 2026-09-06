package com.healthinsurance.risk.controller;

import com.healthinsurance.risk.dto.request.CreateRiskAssessmentRequest;
import com.healthinsurance.risk.dto.response.RiskAssessmentResponse;
import com.healthinsurance.risk.service.RiskAssessmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/risk-assessments")
@RequiredArgsConstructor
public class RiskAssessmentController {

    private final RiskAssessmentService assessmentService;

    @PostMapping
    public ResponseEntity<RiskAssessmentResponse> createAssessment(@Valid @RequestBody CreateRiskAssessmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assessmentService.createAssessment(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RiskAssessmentResponse> getAssessment(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(assessmentService.getAssessment(id));
    }

    @PostMapping("/{id}/calculate")
    public ResponseEntity<RiskAssessmentResponse> calculateRisk(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(assessmentService.calculateRisk(id));
    }
}