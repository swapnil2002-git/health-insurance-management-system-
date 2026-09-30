package com.healthinsurance.quotation.client.fallback;

import com.healthinsurance.quotation.client.ProductClient;
import com.healthinsurance.quotation.client.dto.PlanDto;
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
            public PlanDto getPlan(UUID planId) {
                log.error("Fallback triggered for ProductClient.getPlan({}): {}", planId, cause.getMessage());
                throw new IllegalStateException("Product Plan Service is temporarily unavailable. Cannot verify plan: " + planId, cause);
            }
        };
    }
}
