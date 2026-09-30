package com.healthinsurance.payment.service.impl;

import com.healthinsurance.payment.dto.request.PaymentConfirmRequest;
import com.healthinsurance.payment.dto.request.PaymentInitiateRequest;
import com.healthinsurance.payment.dto.request.RefundRequest;
import com.healthinsurance.payment.dto.response.PaymentResponse;
import com.healthinsurance.payment.dto.response.RefundResponse;
import com.healthinsurance.payment.entity.PaymentAllocation;
import com.healthinsurance.payment.entity.PaymentTransaction;
import com.healthinsurance.payment.entity.RefundTransaction;
import com.healthinsurance.payment.enums.PaymentStatus;
import com.healthinsurance.payment.event.PaymentEventProducer;
import com.healthinsurance.payment.event.PremiumPaidEvent;
import com.healthinsurance.payment.exception.InvalidPaymentStateException;
import com.healthinsurance.payment.exception.PaymentNotFoundException;
import com.healthinsurance.payment.exception.PaymentProcessingException;
import com.healthinsurance.payment.gateway.PaymentGateway;
import com.healthinsurance.payment.gateway.PaymentGatewayResponse;
import com.healthinsurance.payment.mapper.PaymentMapper;
import com.healthinsurance.payment.repository.PaymentAllocationRepository;
import com.healthinsurance.payment.repository.PaymentTransactionRepository;
import com.healthinsurance.payment.repository.RefundTransactionRepository;
import com.healthinsurance.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentTransactionRepository paymentRepository;
    private final PaymentAllocationRepository allocationRepository;
    private final RefundTransactionRepository refundRepository;
    private final PaymentGateway paymentGateway;
    private final PaymentEventProducer eventProducer;
    private final PaymentMapper mapper;

    @Override
    @Transactional
    public PaymentResponse initiatePayment(PaymentInitiateRequest request) {
        log.info("Initiating payment for policy ID: {}, installment ID: {}, amount: {}",
                request.getPolicyId(), request.getInstallmentId(), request.getAmount());

        PaymentTransaction payment = new PaymentTransaction();
        payment.setPolicyId(request.getPolicyId());
        payment.setAmount(request.getAmount());
        payment.setCurrency("INR");
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(PaymentStatus.INITIATED);
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());

        // Call Payment Gateway abstraction
        PaymentGatewayResponse gatewayResp = paymentGateway.initiatePayment(
                payment.getPaymentId(), payment.getAmount(), payment.getPaymentMethod()
        );

        if (gatewayResp.isSuccess()) {
            payment.setGatewayReference(gatewayResp.getGatewayReference());
            payment.setStatus(PaymentStatus.PENDING);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(gatewayResp.getErrorMessage());
        }

        // Temporarily store targeted installment in an allocation placeholder
        if (request.getInstallmentId() != null) {
            PaymentAllocation allocation = new PaymentAllocation();
            allocation.setPayment(payment);
            allocation.setInstallmentId(request.getInstallmentId());
            allocation.setAllocatedAmount(request.getAmount());
            allocation.setAllocatedAt(Instant.now());
            payment.getAllocations().add(allocation);
        }

        PaymentTransaction saved = paymentRepository.save(payment);
        log.info("Payment initiated successfully with ID: {}, Status: {}, GatewayRef: {}",
                saved.getPaymentId(), saved.getStatus(), saved.getGatewayReference());

        return mapper.toPaymentResponse(saved);
    }

    @Override
    @Transactional
    public PaymentResponse confirmPayment(UUID paymentId, PaymentConfirmRequest request) {
        log.info("Confirming payment ID: {}, isSuccess: {}", paymentId, request.isSuccess());

        PaymentTransaction payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment transaction not found with ID: " + paymentId));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new InvalidPaymentStateException("Payment is already confirmed and SUCCESS.");
        }
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new InvalidPaymentStateException("Cannot confirm a refunded payment.");
        }

        PaymentGatewayResponse gatewayResp = paymentGateway.confirmPayment(
                payment.getGatewayReference(), request.isSuccess(), request.getFailureReason()
        );

        if (gatewayResp.isSuccess()) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setUpdatedAt(Instant.now());

            // Section 13: Payment Allocation
            UUID targetInstallmentId = null;
            if (!payment.getAllocations().isEmpty()) {
                PaymentAllocation existingAlloc = payment.getAllocations().get(0);
                targetInstallmentId = existingAlloc.getInstallmentId();
            }

            PaymentTransaction saved = paymentRepository.save(payment);
            log.info("Payment confirmed SUCCESS for ID: {}. Triggering PremiumPaid Kafka event.", saved.getPaymentId());

            // Section 14 & 15: Publish PremiumPaid Kafka Event
            PremiumPaidEvent event = new PremiumPaidEvent(
                    "PREMIUM_PAID",
                    saved.getPaymentId(),
                    saved.getPolicyId(),
                    targetInstallmentId,
                    saved.getAmount(),
                    "SUCCESS",
                    saved.getUpdatedAt(),
                    saved.getGatewayReference(),
                    Instant.now()
            );
            eventProducer.publishPremiumPaid(event);

            return mapper.toPaymentResponse(saved);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(gatewayResp.getErrorMessage());
            payment.setUpdatedAt(Instant.now());
            PaymentTransaction saved = paymentRepository.save(payment);
            log.warn("Payment confirmation FAILED for ID: {}. Reason: {}", saved.getPaymentId(), saved.getFailureReason());
            return mapper.toPaymentResponse(saved);
        }
    }

    @Override
    @Transactional
    public RefundResponse processRefund(UUID paymentId, RefundRequest request) {
        log.info("Processing refund for payment ID: {}, amount: {}", paymentId, request.getAmount());

        PaymentTransaction payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment transaction not found with ID: " + paymentId));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new InvalidPaymentStateException("Only SUCCESS payments can be refunded. Current status: " + payment.getStatus());
        }

        if (request.getAmount().compareTo(payment.getAmount()) > 0) {
            throw new PaymentProcessingException("Refund amount (" + request.getAmount() + ") cannot exceed original payment amount (" + payment.getAmount() + ")");
        }

        PaymentGatewayResponse gatewayResp = paymentGateway.processRefund(paymentId, request.getAmount(), request.getReason());

        if (!gatewayResp.isSuccess()) {
            throw new PaymentProcessingException("Gateway failed to process refund: " + gatewayResp.getErrorMessage());
        }

        RefundTransaction refund = new RefundTransaction();
        refund.setPayment(payment);
        refund.setAmount(request.getAmount());
        refund.setReason(request.getReason());
        refund.setRefundReference(gatewayResp.getGatewayReference());
        refund.setCreatedAt(Instant.now());

        RefundTransaction savedRefund = refundRepository.save(refund);

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setUpdatedAt(Instant.now());
        paymentRepository.save(payment);

        log.info("Successfully processed refund ID: {} for Payment ID: {}", savedRefund.getRefundId(), paymentId);
        return mapper.toRefundResponse(savedRefund);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID paymentId) {
        PaymentTransaction payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));
        return mapper.toPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByPolicy(UUID policyId) {
        List<PaymentTransaction> payments = paymentRepository.findByPolicyIdOrderByCreatedAtDesc(policyId);
        return mapper.toPaymentResponses(payments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        List<PaymentTransaction> payments = paymentRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        return mapper.toPaymentResponses(payments);
    }
}