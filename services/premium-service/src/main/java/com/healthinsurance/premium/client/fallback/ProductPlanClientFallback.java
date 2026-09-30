package com.healthinsurance.premium.client.fallback;

import com.healthinsurance.premium.client.ProductPlanClient;
import com.healthinsurance.premium.client.dto.PlanResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class ProductPlanClientFallback implements FallbackFactory<ProductPlanClient> {

    @Override
    public ProductPlanClient create(Throwable cause) {
        return new ProductPlanClient() {
            @Override
            public PlanResponseDto getPlan(UUID planId) {
                log.error("Fallback triggered for Premium ProductPlanClient.getPlan({}): {}", planId, cause.getMessage());
                throw new IllegalStateException("Product Plan Service is temporarily unavailable for premium calculation: " + planId, cause);
            }
        };
    }
}
