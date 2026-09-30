package com.healthinsurance.claims.client.fallback;

import com.healthinsurance.claims.client.ProviderServiceClient;
import com.healthinsurance.claims.client.dto.ProviderClientDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class ProviderServiceClientFallback implements FallbackFactory<ProviderServiceClient> {

    @Override
    public ProviderServiceClient create(Throwable cause) {
        return new ProviderServiceClient() {
            @Override
            public ProviderClientDto getProviderById(UUID providerId) {
                log.error("Fallback triggered for ProviderServiceClient.getProviderById({}): {}", providerId, cause.getMessage());
                throw new IllegalStateException("Provider Service is temporarily unavailable. Cannot verify provider: " + providerId, cause);
            }
        };
    }
}
