package com.healthinsurance.claims.client;

import com.healthinsurance.claims.client.dto.ProviderClientDto;
import com.healthinsurance.claims.client.fallback.ProviderServiceClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "provider-service", path = "/api/providers", fallbackFactory = ProviderServiceClientFallback.class)
public interface ProviderServiceClient {

    @GetMapping("/{providerId}")
    ProviderClientDto getProviderById(@PathVariable("providerId") UUID providerId);
}
