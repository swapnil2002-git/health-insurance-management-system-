package com.healthinsurance.underwriting.repository;

import com.healthinsurance.underwriting.entity.UnderwritingCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UnderwritingCaseRepository extends JpaRepository<UnderwritingCase, UUID> {
    // Used for idempotency check during Kafka event consumption
    Optional<UnderwritingCase> findByAssessmentId(UUID assessmentId);
}