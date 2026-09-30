package com.healthinsurance.payment.service;

import com.healthinsurance.payment.dto.request.PaymentConfirmRequest;
import com.healthinsurance.payment.dto.request.PaymentInitiateRequest;
import com.healthinsurance.payment.dto.request.RefundRequest;
import com.healthinsurance.payment.dto.response.PaymentResponse;
import com.healthinsurance.payment.dto.response.RefundResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    PaymentResponse initiatePayment(PaymentInitiateRequest request);

    PaymentResponse confirmPayment(UUID paymentId, PaymentConfirmRequest request);

    RefundResponse processRefund(UUID paymentId, RefundRequest request);

    PaymentResponse getPayment(UUID paymentId);

    List<PaymentResponse> getPaymentsByPolicy(UUID policyId);

    List<PaymentResponse> getAllPayments();
}