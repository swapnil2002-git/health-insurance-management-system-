package com.healthinsurance.policy.client;

import com.healthinsurance.policy.client.dto.CustomerMemberDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "customer-service", path = "/api/customers", url = "${customer.service.url:http://localhost:8083}")
public interface CustomerClient {
    @GetMapping("/{customerId}/members")
    List<CustomerMemberDto> getMembersByCustomerId(@PathVariable("customerId") UUID customerId);
}
