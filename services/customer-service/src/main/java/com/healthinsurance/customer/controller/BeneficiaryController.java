package com.healthinsurance.customer.controller;

import com.healthinsurance.customer.dto.request.BeneficiaryRequest;
import com.healthinsurance.customer.dto.response.BeneficiaryResponse;
import com.healthinsurance.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers/{customerId}/beneficiaries")
@RequiredArgsConstructor
public class BeneficiaryController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<BeneficiaryResponse> addBeneficiary(
            @PathVariable("customerId") UUID customerId,
            @Valid @RequestBody BeneficiaryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.addBeneficiary(customerId, request));
    }

    @GetMapping
    public ResponseEntity<List<BeneficiaryResponse>> getBeneficiaries(@PathVariable("customerId") UUID customerId) {
        return ResponseEntity.ok(customerService.getBeneficiaries(customerId));
    }

    @PutMapping("/{beneficiaryId}")
    public ResponseEntity<BeneficiaryResponse> updateBeneficiary(
            @PathVariable("customerId") UUID customerId,
            @PathVariable("beneficiaryId") UUID beneficiaryId,
            @Valid @RequestBody BeneficiaryRequest request) {
        return ResponseEntity.ok(customerService.updateBeneficiary(customerId, beneficiaryId, request));
    }

    @DeleteMapping("/{beneficiaryId}")
    public ResponseEntity<Void> removeBeneficiary(
            @PathVariable("customerId") UUID customerId,
            @PathVariable("beneficiaryId") UUID beneficiaryId) {
        customerService.removeBeneficiary(customerId, beneficiaryId);
        return ResponseEntity.noContent().build();
    }
}