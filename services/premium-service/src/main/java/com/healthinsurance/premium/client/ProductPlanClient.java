package com.healthinsurance.premium.client;

import com.healthinsurance.premium.client.dto.PlanResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "product-plan-service", path = "/api/plans")
public interface ProductPlanClient {

    @GetMapping("/{planId}")
    PlanResponseDto getPlan(@PathVariable("planId") UUID planId);
}