package com.healthinsurance.claims.repository;

import com.healthinsurance.claims.entity.ClaimValidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClaimValidationRepository extends JpaRepository<ClaimValidation, UUID> {

    List<ClaimValidation> findByClaim_ClaimId(UUID claimId);
}
