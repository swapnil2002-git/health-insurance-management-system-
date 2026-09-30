package com.healthinsurance.policy.client.fallback;

import com.healthinsurance.policy.client.PremiumClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class PremiumClientFallback implements FallbackFactory<PremiumClient> {

    @Override
    public PremiumClient create(Throwable cause) {
        return new PremiumClient() {
            @Override
            public Map<String, Object> getPremiumByPolicy(UUID policyId) {
                log.error("Fallback triggered for PremiumClient.getPremiumByPolicy({}): {}", policyId, cause.getMessage());
                throw new IllegalStateException("Premium Service is temporarily unavailable for premium lookup: " + policyId, cause);
            }

            @Override
            public Map<String, Object> recalculatePremium(UUID policyId, Map<String, Object> request) {
                log.error("Fallback triggered for PremiumClient.recalculatePremium({}): {}", policyId, cause.getMessage());
                throw new IllegalStateException("Premium Service is temporarily unavailable for recalculation: " + policyId, cause);
            }
        };
    }
}
