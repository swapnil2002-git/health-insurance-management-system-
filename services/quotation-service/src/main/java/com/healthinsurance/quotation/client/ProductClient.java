package com.healthinsurance.quotation.client;

import com.healthinsurance.quotation.client.dto.PlanDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@FeignClient(name = "product-plan-service")
public interface ProductClient {
    @GetMapping("/api/plans/{planId}")
    PlanDto getPlan(@PathVariable("planId") UUID planId);
}