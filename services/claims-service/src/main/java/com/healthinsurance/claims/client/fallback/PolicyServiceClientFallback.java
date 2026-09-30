package com.healthinsurance.claims.client.fallback;

import com.healthinsurance.claims.client.PolicyServiceClient;
import com.healthinsurance.claims.client.dto.PolicyClientDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class PolicyServiceClientFallback implements FallbackFactory<PolicyServiceClient> {

    @Override
    public PolicyServiceClient create(Throwable cause) {
        return new PolicyServiceClient() {
            @Override
            public PolicyClientDto getPolicyById(UUID id) {
                log.error("Fallback triggered for PolicyServiceClient.getPolicyById({}): {}", id, cause.getMessage());
                throw new IllegalStateException("Policy Service is temporarily unavailable. Cannot verify policy: " + id, cause);
            }
        };
    }
}
