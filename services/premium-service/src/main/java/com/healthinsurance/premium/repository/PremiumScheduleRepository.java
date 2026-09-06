package com.healthinsurance.premium.repository;

import com.healthinsurance.premium.entity.PremiumSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PremiumScheduleRepository extends JpaRepository<PremiumSchedule, UUID> {
    Optional<PremiumSchedule> findByPolicyId(UUID policyId);
    boolean existsByPolicyId(UUID policyId);
}