package com.healthinsurance.claims.repository;

import com.healthinsurance.claims.entity.ExplanationOfBenefits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExplanationOfBenefitsRepository extends JpaRepository<ExplanationOfBenefits, UUID> {

    Optional<ExplanationOfBenefits> findByClaim_ClaimId(UUID claimId);

    Optional<ExplanationOfBenefits> findByEobNumber(String eobNumber);
}
