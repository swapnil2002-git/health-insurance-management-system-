package com.healthinsurance.claims.repository;

import com.healthinsurance.claims.entity.ClaimService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClaimServiceRepository extends JpaRepository<ClaimService, UUID> {

    List<ClaimService> findByClaim_ClaimId(UUID claimId);
}
