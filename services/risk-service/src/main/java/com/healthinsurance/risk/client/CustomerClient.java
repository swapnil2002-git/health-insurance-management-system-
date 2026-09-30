package com.healthinsurance.risk.client;
import com.healthinsurance.risk.client.dto.DependencyDto;
import com.healthinsurance.risk.client.fallback.CustomerClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@FeignClient(name = "customer-service", url = "${customer.service.url:http://localhost:8083}", fallbackFactory = CustomerClientFallback.class)
public interface CustomerClient {
    @GetMapping("/api/customers/{customerId}")
    DependencyDto getCustomer(@PathVariable("customerId") UUID customerId);
}