package com.healthinsurance.policy.client;

import com.healthinsurance.policy.client.fallback.PremiumClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "premium-service", fallbackFactory = PremiumClientFallback.class)
public interface PremiumClient {

    @GetMapping("/api/policies/{policyId}/premium")
    Map<String, Object> getPremiumByPolicy(@PathVariable("policyId") UUID policyId);

    @PostMapping("/api/policies/{policyId}/premium/recalculate")
    Map<String, Object> recalculatePremium(
            @PathVariable("policyId") UUID policyId,
            @RequestBody Map<String, Object> request);
}
