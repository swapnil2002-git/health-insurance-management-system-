package com.healthinsurance.quotation.client;

import com.healthinsurance.quotation.client.dto.CustomerDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@FeignClient(name = "customer-service")
public interface CustomerClient {
    @GetMapping("/api/customers/{customerId}")
    CustomerDto getCustomer(@PathVariable("customerId") UUID customerId);
}