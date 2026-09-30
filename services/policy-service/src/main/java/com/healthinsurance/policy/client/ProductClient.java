package com.healthinsurance.policy.client;
import com.healthinsurance.policy.client.dto.PlanDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@FeignClient(name = "product-plan-service", path = "/api/plans", url = "${product.service.url:http://localhost:8084}")
public interface ProductClient {
    @GetMapping("/{id}")
    PlanDto getPlanById(@PathVariable("id") UUID id);
}