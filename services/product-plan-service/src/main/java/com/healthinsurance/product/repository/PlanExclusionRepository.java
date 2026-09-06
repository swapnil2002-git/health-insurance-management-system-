package com.healthinsurance.product.repository;

import com.healthinsurance.product.entity.PlanExclusion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface PlanExclusionRepository extends JpaRepository<PlanExclusion, UUID> {
    List<PlanExclusion> findByPlan_PlanId(UUID planId);
}