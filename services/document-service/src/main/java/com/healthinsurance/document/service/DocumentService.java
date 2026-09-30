package com.healthinsurance.document.service;

import com.healthinsurance.document.domain.DocumentStatus;
import com.healthinsurance.document.dto.DocumentResponse;
import com.healthinsurance.document.dto.DocumentUploadRequest;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.UUID;

public interface DocumentService {

    DocumentResponse uploadDocument(DocumentUploadRequest request);

    DocumentResponse getDocumentById(UUID documentId);

    Resource downloadDocument(UUID documentId);

    List<DocumentResponse> getDocumentsByReferenceId(UUID referenceId);

    DocumentResponse updateDocumentStatus(UUID documentId, DocumentStatus status);
}
