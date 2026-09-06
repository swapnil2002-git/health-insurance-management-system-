package com.healthinsurance.product.repository;

import com.healthinsurance.product.entity.InsuranceProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface InsuranceProductRepository extends JpaRepository<InsuranceProduct, UUID> {}