package com.healthinsurance.claims.client;

import com.healthinsurance.claims.client.dto.PolicyClientDto;
import com.healthinsurance.claims.client.fallback.PolicyServiceClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "policy-service", path = "/api/policies", fallbackFactory = PolicyServiceClientFallback.class)
public interface PolicyServiceClient {

    @GetMapping("/{id}")
    PolicyClientDto getPolicyById(@PathVariable("id") UUID id);
}
