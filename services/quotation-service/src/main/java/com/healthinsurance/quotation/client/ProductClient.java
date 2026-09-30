package com.healthinsurance.quotation.client;

import com.healthinsurance.quotation.client.dto.PlanDto;
import com.healthinsurance.quotation.client.fallback.ProductClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@FeignClient(name = "product-plan-service", url = "${product.service.url:http://localhost:8084}", fallbackFactory = ProductClientFallback.class)
public interface ProductClient {
    @GetMapping("/api/plans/{planId}")
    PlanDto getPlan(@PathVariable("planId") UUID planId);
}