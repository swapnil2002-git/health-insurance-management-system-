package com.healthinsurance.customer.controller;

import com.healthinsurance.customer.dto.request.NomineeRequest;
import com.healthinsurance.customer.dto.response.NomineeResponse;
import com.healthinsurance.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers/{customerId}/nominees")
@RequiredArgsConstructor
public class NomineeController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<NomineeResponse> addNominee(
            @PathVariable("customerId") UUID customerId,
            @Valid @RequestBody NomineeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.addNominee(customerId, request));
    }

    @GetMapping
    public ResponseEntity<List<NomineeResponse>> getNominees(@PathVariable("customerId") UUID customerId) {
        return ResponseEntity.ok(customerService.getNominees(customerId));
    }

    @PutMapping("/{nomineeId}")
    public ResponseEntity<NomineeResponse> updateNominee(
            @PathVariable("customerId") UUID customerId,
            @PathVariable("nomineeId") UUID nomineeId,
            @Valid @RequestBody NomineeRequest request) {
        return ResponseEntity.ok(customerService.updateNominee(customerId, nomineeId, request));
    }

    @DeleteMapping("/{nomineeId}")
    public ResponseEntity<Void> removeNominee(
            @PathVariable("customerId") UUID customerId,
            @PathVariable("nomineeId") UUID nomineeId) {
        customerService.removeNominee(customerId, nomineeId);
        return ResponseEntity.noContent().build();
    }
}