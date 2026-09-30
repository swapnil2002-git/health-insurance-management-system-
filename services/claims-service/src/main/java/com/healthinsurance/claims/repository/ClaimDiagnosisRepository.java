package com.healthinsurance.claims.repository;

import com.healthinsurance.claims.entity.ClaimDiagnosis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClaimDiagnosisRepository extends JpaRepository<ClaimDiagnosis, UUID> {

    List<ClaimDiagnosis> findByClaim_ClaimId(UUID claimId);
}
