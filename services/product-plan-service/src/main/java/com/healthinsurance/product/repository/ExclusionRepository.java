package com.healthinsurance.product.repository;

import com.healthinsurance.product.entity.Exclusion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ExclusionRepository extends JpaRepository<Exclusion, UUID> {}