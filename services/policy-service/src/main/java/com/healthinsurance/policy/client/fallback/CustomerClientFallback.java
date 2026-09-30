package com.healthinsurance.policy.client.fallback;

import com.healthinsurance.policy.client.CustomerClient;
import com.healthinsurance.policy.client.dto.CustomerMemberDto;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class CustomerClientFallback implements CustomerClient {
    @Override
    public List<CustomerMemberDto> getMembersByCustomerId(UUID customerId) {
        return Collections.emptyList();
    }
}
