package com.healthinsurance.claims.resilience;

import com.healthinsurance.claims.client.PolicyServiceClient;
import com.healthinsurance.claims.client.fallback.PolicyServiceClientFallback;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClaimsResilienceIntegrationTest {

    @Test
    void testPolicyServiceClientFallback_ThrowsControlledExceptionWithoutFabricatingData() {
        PolicyServiceClientFallback fallbackFactory = new PolicyServiceClientFallback();
        PolicyServiceClient client = fallbackFactory.create(new RuntimeException("Simulated Connection Timeout"));

        UUID samplePolicyId = UUID.randomUUID();
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> client.getPolicyById(samplePolicyId));
        assertTrue(ex.getMessage().contains("Policy Service is temporarily unavailable"));
    }
}
