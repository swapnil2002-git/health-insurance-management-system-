package com.healthinsurance.policy.repository;

import com.healthinsurance.policy.entity.PolicyCancellation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface PolicyCancellationRepository extends JpaRepository<PolicyCancellation, UUID> {
}