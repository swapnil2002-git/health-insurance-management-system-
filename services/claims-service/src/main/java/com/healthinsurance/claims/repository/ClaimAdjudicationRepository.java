package com.healthinsurance.claims.repository;

import com.healthinsurance.claims.entity.ClaimAdjudication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClaimAdjudicationRepository extends JpaRepository<ClaimAdjudication, UUID> {

    Optional<ClaimAdjudication> findByClaim_ClaimId(UUID claimId);
}
