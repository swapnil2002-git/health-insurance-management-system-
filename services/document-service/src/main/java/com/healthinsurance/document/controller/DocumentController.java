package com.healthinsurance.document.controller;

import com.healthinsurance.document.domain.DocumentStatus;
import com.healthinsurance.document.domain.DocumentType;
import com.healthinsurance.document.dto.DocumentResponse;
import com.healthinsurance.document.dto.DocumentUploadRequest;
import com.healthinsurance.document.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Document Management", description = "Endpoints for uploading, downloading, and querying healthcare document metadata and files")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'HEALTHCARE_PROVIDER', 'CLAIMS_OFFICER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(
        summary = "Upload a document file",
        description = "Accepts multipart form-data, securely writes physical file to storage, and records metadata in document-db"
    )
    public ResponseEntity<DocumentResponse> uploadDocument(
            @Parameter(description = "Physical file to upload (PDF, JPEG, PNG)", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Document category type", required = true)
            @RequestParam("documentType") DocumentType documentType,
            @Parameter(description = "Optional business reference ID (e.g. claimId, policyId)")
            @RequestParam(value = "referenceId", required = false) UUID referenceId) {

        log.info("REST request to upload document: type={}, referenceId={}", documentType, referenceId);
        DocumentUploadRequest request = DocumentUploadRequest.builder()
                .file(file)
                .documentType(documentType)
                .referenceId(referenceId)
                .build();

        DocumentResponse response = documentService.uploadDocument(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{documentId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'HEALTHCARE_PROVIDER', 'CLAIMS_OFFICER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Get document metadata by ID", description = "Retrieves stored metadata for a document by its unique UUID")
    public ResponseEntity<DocumentResponse> getDocumentMetadata(@PathVariable("documentId") UUID documentId) {
        log.info("REST request to get document metadata for ID: {}", documentId);
        DocumentResponse response = documentService.getDocumentById(documentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{documentId}/download")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'HEALTHCARE_PROVIDER', 'CLAIMS_OFFICER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(
        summary = "Download physical document file",
        description = "Streams binary file from storage with original filename as download attachment"
    )
    public ResponseEntity<Resource> downloadDocument(@PathVariable("documentId") UUID documentId) {
        log.info("REST request to download document for ID: {}", documentId);
        DocumentResponse metadata = documentService.getDocumentById(documentId);
        Resource fileResource = documentService.downloadDocument(documentId);

        String contentType = metadata.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getFileName() + "\"")
                .body(fileResource);
    }

    @GetMapping("/reference/{referenceId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'HEALTHCARE_PROVIDER', 'CLAIMS_OFFICER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Get documents by reference ID", description = "Retrieves all document metadata associated with a business reference ID (e.g. claimId)")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByReferenceId(@PathVariable("referenceId") UUID referenceId) {
        log.info("REST request to get documents for reference ID: {}", referenceId);
        List<DocumentResponse> responses = documentService.getDocumentsByReferenceId(referenceId);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{documentId}/status")
    @PreAuthorize("hasAnyRole('CLAIMS_OFFICER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @Operation(summary = "Update document status", description = "Updates document lifecycle status (ACTIVE, INACTIVE, ARCHIVED)")
    public ResponseEntity<DocumentResponse> updateDocumentStatus(
            @PathVariable("documentId") UUID documentId,
            @RequestParam("status") DocumentStatus status) {
        log.info("REST request to update status for document ID: {} to {}", documentId, status);
        DocumentResponse response = documentService.updateDocumentStatus(documentId, status);
        return ResponseEntity.ok(response);
    }
}
