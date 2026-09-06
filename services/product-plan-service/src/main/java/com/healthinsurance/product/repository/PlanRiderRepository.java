package com.healthinsurance.product.repository;

import com.healthinsurance.product.entity.PlanRider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface PlanRiderRepository extends JpaRepository<PlanRider, UUID> {
    List<PlanRider> findByPlan_PlanId(UUID planId);
}