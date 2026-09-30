package com.healthinsurance.policy.client.fallback;

import com.healthinsurance.policy.client.PaymentClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class PaymentClientFallback implements FallbackFactory<PaymentClient> {

    @Override
    public PaymentClient create(Throwable cause) {
        return new PaymentClient() {
            @Override
            public List<Map<String, Object>> getPaymentsByPolicy(UUID policyId) {
                log.error("Fallback triggered for PaymentClient.getPaymentsByPolicy({}): {}", policyId, cause.getMessage());
                throw new IllegalStateException("Payment Service is temporarily unavailable for payment lookup: " + policyId, cause);
            }

            @Override
            public Map<String, Object> processRefund(UUID paymentId, Map<String, Object> request) {
                log.error("Fallback triggered for PaymentClient.processRefund({}): {}", paymentId, cause.getMessage());
                // Controlled Fallback: Do NOT fabricate a successful refund!
                throw new IllegalStateException("Payment Service is temporarily unavailable. Refund could not be processed: " + paymentId, cause);
            }
        };
    }
}
