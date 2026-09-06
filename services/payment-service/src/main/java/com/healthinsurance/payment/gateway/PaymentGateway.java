package com.healthinsurance.payment.gateway;

import com.healthinsurance.payment.enums.PaymentMethod;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentGateway {

    PaymentGatewayResponse initiatePayment(UUID paymentId, BigDecimal amount, PaymentMethod method);

    PaymentGatewayResponse confirmPayment(String gatewayReference, boolean isSuccess, String failureReason);

    PaymentGatewayResponse processRefund(UUID paymentId, BigDecimal amount, String reason);
}