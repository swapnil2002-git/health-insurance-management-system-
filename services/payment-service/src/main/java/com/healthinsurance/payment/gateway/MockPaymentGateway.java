package com.healthinsurance.payment.gateway;

import com.healthinsurance.payment.enums.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public PaymentGatewayResponse initiatePayment(UUID paymentId, BigDecimal amount, PaymentMethod method) {
        String reference = "GW-TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("MockPaymentGateway: Initiating payment {} for amount {} via {}. Generated ref: {}",
                paymentId, amount, method, reference);
        return new PaymentGatewayResponse(true, reference, null);
    }

    @Override
    public PaymentGatewayResponse confirmPayment(String gatewayReference, boolean isSuccess, String failureReason) {
        log.info("MockPaymentGateway: Confirming payment for ref: {}. Success: {}", gatewayReference, isSuccess);
        if (isSuccess) {
            return new PaymentGatewayResponse(true, gatewayReference, null);
        } else {
            String error = failureReason != null ? failureReason : "Card declined / Insufficient funds";
            return new PaymentGatewayResponse(false, gatewayReference, error);
        }
    }

    @Override
    public PaymentGatewayResponse processRefund(UUID paymentId, BigDecimal amount, String reason) {
        String refundRef = "REF-TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("MockPaymentGateway: Processing refund for payment {} of amount {}. Generated refund ref: {}",
                paymentId, amount, refundRef);
        return new PaymentGatewayResponse(true, refundRef, null);
    }
}