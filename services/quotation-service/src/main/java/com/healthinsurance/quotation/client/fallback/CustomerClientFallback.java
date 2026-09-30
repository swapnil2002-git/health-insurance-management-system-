package com.healthinsurance.quotation.client.fallback;

import com.healthinsurance.quotation.client.CustomerClient;
import com.healthinsurance.quotation.client.dto.CustomerDto;
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
            public CustomerDto getCustomer(UUID customerId) {
                log.error("Fallback triggered for CustomerClient.getCustomer({}): {}", customerId, cause.getMessage());
                throw new IllegalStateException("Customer Service is temporarily unavailable. Cannot verify customer: " + customerId, cause);
            }
        };
    }
}
