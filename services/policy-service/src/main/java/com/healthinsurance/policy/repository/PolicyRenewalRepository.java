package com.healthinsurance.policy.repository;

import com.healthinsurance.policy.entity.PolicyRenewal;
import com.healthinsurance.policy.enums.RenewalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PolicyRenewalRepository extends JpaRepository<PolicyRenewal, UUID> {

    List<PolicyRenewal> findByPolicy_PolicyId(UUID policyId);

    Optional<PolicyRenewal> findByPolicy_PolicyIdAndStatus(UUID policyId, RenewalStatus status);

    boolean existsByPolicy_PolicyIdAndStatusIn(UUID policyId, Collection<RenewalStatus> statuses);
}
