package com.healthinsurance.underwriting.repository;

import com.healthinsurance.underwriting.entity.UnderwritingDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface UnderwritingDecisionRepository extends JpaRepository<UnderwritingDecision, UUID> {}