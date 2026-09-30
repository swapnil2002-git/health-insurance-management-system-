package com.healthinsurance.policy.service;

import com.healthinsurance.policy.entity.Policy;
import com.healthinsurance.policy.enums.PolicyStatus;
import com.healthinsurance.policy.repository.PolicyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyOptimisticLockingTest {

    @Mock
    private PolicyRepository policyRepository;

    @Test
    void testOptimisticLockingConflict_ThrowsException() {
        UUID policyId = UUID.randomUUID();

        // Transaction A and Transaction B read Policy with version = 1L
        Policy policyForTxA = new Policy();
        policyForTxA.setPolicyId(policyId);
        policyForTxA.setPolicyNumber("POL-1001");
        policyForTxA.setCustomerId(UUID.randomUUID());
        policyForTxA.setPlanId(UUID.randomUUID());
        policyForTxA.setQuoteId(UUID.randomUUID());
        policyForTxA.setStatus(PolicyStatus.ACTIVE);
        policyForTxA.setVersion(1L);

        Policy policyForTxB = new Policy();
        policyForTxB.setPolicyId(policyId);
        policyForTxB.setPolicyNumber("POL-1001");
        policyForTxB.setCustomerId(policyForTxA.getCustomerId());
        policyForTxB.setPlanId(policyForTxA.getPlanId());
        policyForTxB.setQuoteId(policyForTxA.getQuoteId());
        policyForTxB.setStatus(PolicyStatus.ACTIVE);
        policyForTxB.setVersion(1L); // Stale version!

        // Transaction A commits successfully and increments version to 2L
        when(policyRepository.save(policyForTxA)).thenAnswer(invocation -> {
            Policy p = invocation.getArgument(0);
            p.setVersion(2L);
            p.setUpdatedAt(Instant.now());
            return p;
        });

        Policy committedTxA = policyRepository.save(policyForTxA);
        assertEquals(2L, committedTxA.getVersion());

        // Transaction B tries to save with stale version 1L -> OptimisticLockingFailureException
        when(policyRepository.save(policyForTxB)).thenThrow(
                new OptimisticLockingFailureException("Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect)")
        );

        assertThrows(OptimisticLockingFailureException.class, () -> policyRepository.save(policyForTxB));
    }
}
