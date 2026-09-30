package com.healthinsurance.policy.repository;

import com.healthinsurance.policy.entity.PolicyCancellation;
import com.healthinsurance.policy.enums.CancellationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PolicyCancellationRepository extends JpaRepository<PolicyCancellation, UUID> {

    Optional<PolicyCancellation> findByPolicy_PolicyId(UUID policyId);

    boolean existsByPolicy_PolicyIdAndStatusIn(UUID policyId, Collection<CancellationStatus> statuses);
}