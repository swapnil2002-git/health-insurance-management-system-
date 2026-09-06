package com.healthinsurance.product.repository;

import com.healthinsurance.product.entity.PlanCopayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface PlanCopaymentRepository extends JpaRepository<PlanCopayment, UUID> {
    List<PlanCopayment> findByPlan_PlanId(UUID planId);
}