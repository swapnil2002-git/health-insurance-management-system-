package com.healthinsurance.claims.repository;

import com.healthinsurance.claims.entity.ClaimDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClaimDocumentRepository extends JpaRepository<ClaimDocument, UUID> {

    List<ClaimDocument> findByClaim_ClaimId(UUID claimId);

    boolean existsByClaim_ClaimIdAndDocumentType(UUID claimId, String documentType);
}
