package com.healthinsurance.product.controller;

import com.healthinsurance.product.entity.*;
import com.healthinsurance.product.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class MasterRuleController {

    private final CoverageRepository coverageRepository;
    private final DeductibleRepository deductibleRepository;
    private final CopaymentRepository copaymentRepository;
    private final ExclusionRepository exclusionRepository;
    private final RiderRepository riderRepository;

    @GetMapping("/coverages")
    public ResponseEntity<List<Coverage>> getAllCoverages() {
        return ResponseEntity.ok(coverageRepository.findAll());
    }

    @GetMapping("/deductibles")
    public ResponseEntity<List<Deductible>> getAllDeductibles() {
        return ResponseEntity.ok(deductibleRepository.findAll());
    }

    @GetMapping("/copayments")
    public ResponseEntity<List<Copayment>> getAllCopayments() {
        return ResponseEntity.ok(copaymentRepository.findAll());
    }

    @GetMapping("/exclusions")
    public ResponseEntity<List<Exclusion>> getAllExclusions() {
        return ResponseEntity.ok(exclusionRepository.findAll());
    }

    @GetMapping("/riders")
    public ResponseEntity<List<Rider>> getAllRiders() {
        return ResponseEntity.ok(riderRepository.findAll());
    }
}