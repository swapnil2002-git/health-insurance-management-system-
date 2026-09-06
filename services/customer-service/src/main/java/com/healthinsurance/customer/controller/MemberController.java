package com.healthinsurance.customer.controller;

import com.healthinsurance.customer.dto.request.MemberRequest;
import com.healthinsurance.customer.dto.response.MemberResponse;
import com.healthinsurance.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers/{customerId}/members")
@RequiredArgsConstructor
public class MemberController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<MemberResponse> addMember(
            @PathVariable("customerId") UUID customerId,
            @Valid @RequestBody MemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.addMember(customerId, request));
    }

    @GetMapping
    public ResponseEntity<List<MemberResponse>> getMembers(@PathVariable("customerId") UUID customerId) {
        return ResponseEntity.ok(customerService.getMembers(customerId));
    }

    @PutMapping("/{memberId}")
    public ResponseEntity<MemberResponse> updateMember(
            @PathVariable("customerId") UUID customerId,
            @PathVariable("memberId") UUID memberId,
            @Valid @RequestBody MemberRequest request) {
        return ResponseEntity.ok(customerService.updateMember(customerId, memberId, request));
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable("customerId") UUID customerId,
            @PathVariable("memberId") UUID memberId) {
        customerService.removeMember(customerId, memberId);
        return ResponseEntity.noContent().build();
    }
}