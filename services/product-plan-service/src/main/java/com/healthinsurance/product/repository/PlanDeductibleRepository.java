package com.healthinsurance.product.repository;

import com.healthinsurance.product.entity.PlanDeductible;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface PlanDeductibleRepository extends JpaRepository<PlanDeductible, UUID> {
    List<PlanDeductible> findByPlan_PlanId(UUID planId);
}