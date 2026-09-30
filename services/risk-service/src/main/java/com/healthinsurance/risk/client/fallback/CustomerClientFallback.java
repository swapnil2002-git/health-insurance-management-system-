package com.healthinsurance.risk.client.fallback;

import com.healthinsurance.risk.client.CustomerClient;
import com.healthinsurance.risk.client.dto.DependencyDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class CustomerClientFallback implements FallbackFactory<CustomerClient> {

    @Override
    public CustomerClient create(Throwable cause) {
        return new CustomerClient() {
            @Override
            public DependencyDto getCustomer(UUID customerId) {
                log.error("Fallback triggered for Risk CustomerClient.getCustomer({}): {}", customerId, cause.getMessage());
                throw new IllegalStateException("Customer Service is temporarily unavailable for risk assessment: " + customerId, cause);
            }
        };
    }
}
