package com.healthinsurance.document.service.impl;

import com.healthinsurance.document.domain.DocumentStatus;
import com.healthinsurance.document.dto.DocumentResponse;
import com.healthinsurance.document.dto.DocumentUploadRequest;
import com.healthinsurance.document.entity.Document;
import com.healthinsurance.document.exception.DocumentNotFoundException;
import com.healthinsurance.document.exception.InvalidDocumentException;
import com.healthinsurance.document.mapper.DocumentMapper;
import com.healthinsurance.document.repository.DocumentRepository;
import com.healthinsurance.document.service.DocumentService;
import com.healthinsurance.document.storage.DocumentStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentStorageService storageService;
    private final DocumentMapper documentMapper;

    @Value("${document.storage.max-file-size-bytes:10485760}")
    private long maxFileSizeBytes;

    @Value("${document.storage.allowed-content-types:application/pdf,image/jpeg,image/png,image/jpg}")
    private List<String> allowedContentTypes;

    @Override
    public DocumentResponse uploadDocument(DocumentUploadRequest request) {
        MultipartFile file = request.getFile();
        validateFile(file);

        UUID documentId = UUID.randomUUID();
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String contentType = file.getContentType();
        long fileSize = file.getSize();

        log.info("Uploading document: {} of type: {}, size: {} bytes, generated ID: {}",
                originalFileName, request.getDocumentType(), fileSize, documentId);

        // Step 1: Store physical file safely
        String storageReference = storageService.storeFile(file, documentId);

        // Step 2: Persist metadata in document-db with compensating cleanup on failure
        try {
            Document document = new Document();
            document.setDocumentId(documentId);
            document.setFileName(originalFileName);
            document.setContentType(contentType);
            document.setFileSize(fileSize);
            document.setStorageReference(storageReference);
            document.setDocumentType(request.getDocumentType());
            document.setReferenceId(request.getReferenceId());
            document.setStatus(DocumentStatus.ACTIVE);

            Document saved = documentRepository.save(document);
            log.info("Document metadata saved successfully for ID: {}", saved.getDocumentId());
            return documentMapper.toResponse(saved);
        } catch (Exception ex) {
            log.error("Failed to save document metadata in database for ID: {}. Rolling back physical file.", documentId, ex);
            storageService.deleteFile(storageReference);
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(UUID documentId) {
        log.info("Fetching document metadata for ID: {}", documentId);
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + documentId));
        return documentMapper.toResponse(document);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadDocument(UUID documentId) {
        log.info("Downloading physical document for ID: {}", documentId);
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + documentId));

        if (document.getStatus() == DocumentStatus.ARCHIVED) {
            throw new InvalidDocumentException("Document is ARCHIVED and cannot be downloaded: " + documentId);
        }

        return storageService.loadFileAsResource(document.getStorageReference());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByReferenceId(UUID referenceId) {
        log.info("Fetching documents for reference ID: {}", referenceId);
        List<Document> documents = documentRepository.findByReferenceId(referenceId);
        return documentMapper.toResponseList(documents);
    }

    @Override
    @Transactional
    public DocumentResponse updateDocumentStatus(UUID documentId, DocumentStatus status) {
        log.info("Updating document status for ID: {} to {}", documentId, status);
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + documentId));

        document.setStatus(status);
        Document updated = documentRepository.save(document);
        return documentMapper.toResponse(updated);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidDocumentException("File cannot be empty or null");
        }

        if (file.getSize() > maxFileSizeBytes) {
            throw new InvalidDocumentException(String.format("File size %d exceeds maximum limit of %d bytes",
                    file.getSize(), maxFileSizeBytes));
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedContentTypes.contains(contentType.toLowerCase())) {
            throw new InvalidDocumentException("Unsupported file content type: " + contentType +
                    ". Allowed types: " + allowedContentTypes);
        }
    }
}
