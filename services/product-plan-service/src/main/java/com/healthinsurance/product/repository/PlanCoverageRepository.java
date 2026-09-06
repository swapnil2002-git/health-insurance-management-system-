package com.healthinsurance.product.repository;

import com.healthinsurance.product.entity.PlanCoverage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface PlanCoverageRepository extends JpaRepository<PlanCoverage, UUID> {
    List<PlanCoverage> findByPlan_PlanId(UUID planId);
}