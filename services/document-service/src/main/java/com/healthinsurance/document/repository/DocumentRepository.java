package com.healthinsurance.document.repository;

import com.healthinsurance.document.domain.DocumentStatus;
import com.healthinsurance.document.domain.DocumentType;
import com.healthinsurance.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    List<Document> findByReferenceId(UUID referenceId);

    List<Document> findByReferenceIdAndStatus(UUID referenceId, DocumentStatus status);

    List<Document> findByDocumentType(DocumentType documentType);

    List<Document> findByStatus(DocumentStatus status);

    Optional<Document> findByDocumentIdAndStatus(UUID documentId, DocumentStatus status);
}
