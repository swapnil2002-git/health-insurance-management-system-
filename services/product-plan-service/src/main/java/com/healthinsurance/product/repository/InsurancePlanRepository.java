package com.healthinsurance.product.repository;

import com.healthinsurance.product.entity.InsurancePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface InsurancePlanRepository extends JpaRepository<InsurancePlan, UUID> {
    List<InsurancePlan> findByProduct_ProductId(UUID productId);
}