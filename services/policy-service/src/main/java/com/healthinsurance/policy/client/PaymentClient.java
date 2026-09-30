package com.healthinsurance.policy.client;

import com.healthinsurance.policy.client.fallback.PaymentClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "payment-service", fallbackFactory = PaymentClientFallback.class)
public interface PaymentClient {

    @GetMapping("/api/payments/policy/{policyId}")
    List<Map<String, Object>> getPaymentsByPolicy(@PathVariable("policyId") UUID policyId);

    @PostMapping("/api/payments/{paymentId}/refund")
    Map<String, Object> processRefund(
            @PathVariable("paymentId") UUID paymentId,
            @RequestBody Map<String, Object> request);
}
