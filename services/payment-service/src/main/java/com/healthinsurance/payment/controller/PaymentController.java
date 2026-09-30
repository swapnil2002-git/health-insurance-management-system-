package com.healthinsurance.payment.controller;

import com.healthinsurance.payment.dto.request.PaymentConfirmRequest;
import com.healthinsurance.payment.dto.request.PaymentInitiateRequest;
import com.healthinsurance.payment.dto.request.RefundRequest;
import com.healthinsurance.payment.dto.response.PaymentResponse;
import com.healthinsurance.payment.dto.response.RefundResponse;
import com.healthinsurance.payment.service.PaymentService;
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
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Controller", description = "Endpoints for Payment Initiation, Confirmation, Allocation, and Refunds")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'POLICY_ADMINISTRATOR', 'FINANCE_OFFICER', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Initiate a new payment transaction")
    public ResponseEntity<PaymentResponse> initiatePayment(@Valid @RequestBody PaymentInitiateRequest request) {
        return new ResponseEntity<>(paymentService.initiatePayment(request), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'FINANCE_OFFICER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Confirm payment after gateway processing (Triggers allocation and PremiumPaid Kafka event)")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @PathVariable("id") UUID id,
            @Valid @RequestBody PaymentConfirmRequest request) {
        return ResponseEntity.ok(paymentService.confirmPayment(id, request));
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('FINANCE_OFFICER', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Process refund for a successful payment")
    public ResponseEntity<RefundResponse> processRefund(
            @PathVariable("id") UUID id,
            @Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(paymentService.processRefund(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'FINANCE_OFFICER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Get payment transaction by ID")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }

    @GetMapping("/policy/{policyId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'FINANCE_OFFICER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Get all payment transactions for a policy")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByPolicy(@PathVariable("policyId") UUID policyId) {
        return ResponseEntity.ok(paymentService.getPaymentsByPolicy(policyId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'FINANCE_OFFICER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Get all payment transactions")
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }
}