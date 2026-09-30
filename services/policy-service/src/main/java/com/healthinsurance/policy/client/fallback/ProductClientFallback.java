package com.healthinsurance.policy.client.fallback;

import com.healthinsurance.policy.client.ProductClient;
import com.healthinsurance.policy.client.dto.PlanDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class ProductClientFallback implements FallbackFactory<ProductClient> {

    @Override
    public ProductClient create(Throwable cause) {
        return new ProductClient() {
            @Override
            public PlanDto getPlanById(UUID id) {
                log.error("Fallback triggered for Policy ProductClient.getPlanById({}): {}", id, cause.getMessage());
                throw new IllegalStateException("Product Plan Service is temporarily unavailable for policy lookup: " + id, cause);
            }
        };
    }
}
