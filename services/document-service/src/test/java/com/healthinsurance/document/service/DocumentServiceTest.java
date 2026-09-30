package com.healthinsurance.document.service;

import com.healthinsurance.document.domain.DocumentStatus;
import com.healthinsurance.document.domain.DocumentType;
import com.healthinsurance.document.dto.DocumentResponse;
import com.healthinsurance.document.dto.DocumentUploadRequest;
import com.healthinsurance.document.entity.Document;
import com.healthinsurance.document.exception.DocumentNotFoundException;
import com.healthinsurance.document.exception.InvalidDocumentException;
import com.healthinsurance.document.mapper.DocumentMapper;
import com.healthinsurance.document.repository.DocumentRepository;
import com.healthinsurance.document.service.impl.DocumentServiceImpl;
import com.healthinsurance.document.storage.DocumentStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private DocumentStorageService storageService;
    @Mock
    private DocumentMapper documentMapper;

    @InjectMocks
    private DocumentServiceImpl documentService;

    private UUID documentId;
    private UUID referenceId;
    private Document document;
    private DocumentResponse documentResponse;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(documentService, "maxFileSizeBytes", 10485760L);
        ReflectionTestUtils.setField(documentService, "allowedContentTypes",
                List.of("application/pdf", "image/jpeg", "image/png", "image/jpg"));

        documentId = UUID.randomUUID();
        referenceId = UUID.randomUUID();

        document = new Document();
        document.setDocumentId(documentId);
        document.setFileName("claim-invoice.pdf");
        document.setContentType("application/pdf");
        document.setFileSize(2048L);
        document.setStorageReference(documentId.toString() + ".pdf");
        document.setDocumentType(DocumentType.CLAIM_DOCUMENT);
        document.setReferenceId(referenceId);
        document.setStatus(DocumentStatus.ACTIVE);

        documentResponse = DocumentResponse.builder()
                .documentId(documentId)
                .fileName("claim-invoice.pdf")
                .contentType("application/pdf")
                .fileSize(2048L)
                .documentType(DocumentType.CLAIM_DOCUMENT)
                .referenceId(referenceId)
                .status(DocumentStatus.ACTIVE)
                .build();
    }

    @Test
    void testUploadDocument_Success() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "claim-invoice.pdf", "application/pdf", "PDF test data".getBytes()
        );
        DocumentUploadRequest request = DocumentUploadRequest.builder()
                .file(file)
                .documentType(DocumentType.CLAIM_DOCUMENT)
                .referenceId(referenceId)
                .build();

        when(storageService.storeFile(any(), any())).thenReturn(documentId.toString() + ".pdf");
        when(documentRepository.save(any(Document.class))).thenReturn(document);
        when(documentMapper.toResponse(any(Document.class))).thenReturn(documentResponse);

        DocumentResponse response = documentService.uploadDocument(request);

        assertNotNull(response);
        assertEquals(documentId, response.getDocumentId());
        assertEquals(DocumentType.CLAIM_DOCUMENT, response.getDocumentType());
        assertEquals(referenceId, response.getReferenceId());
        verify(storageService, times(1)).storeFile(any(), any());
        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    void testUploadDocument_DatabaseError_TriggersCompensatingDelete() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "claim-invoice.pdf", "application/pdf", "PDF test data".getBytes()
        );
        DocumentUploadRequest request = DocumentUploadRequest.builder()
                .file(file)
                .documentType(DocumentType.CLAIM_DOCUMENT)
                .referenceId(referenceId)
                .build();

        String storageRef = documentId.toString() + ".pdf";
        when(storageService.storeFile(any(), any())).thenReturn(storageRef);
        when(documentRepository.save(any(Document.class))).thenThrow(new RuntimeException("DB down"));

        assertThrows(RuntimeException.class, () -> documentService.uploadDocument(request));

        // Verify compensating deletion of the physical file
        verify(storageService, times(1)).deleteFile(storageRef);
    }

    @Test
    void testUploadDocument_UnsupportedContentType_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "script.sh", "application/x-sh", "echo hello".getBytes()
        );
        DocumentUploadRequest request = DocumentUploadRequest.builder()
                .file(file)
                .documentType(DocumentType.CLAIM_DOCUMENT)
                .build();

        assertThrows(InvalidDocumentException.class, () -> documentService.uploadDocument(request));
        verifyNoInteractions(storageService);
        verifyNoInteractions(documentRepository);
    }

    @Test
    void testGetDocumentById_Success() {
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
        when(documentMapper.toResponse(document)).thenReturn(documentResponse);

        DocumentResponse response = documentService.getDocumentById(documentId);

        assertNotNull(response);
        assertEquals(documentId, response.getDocumentId());
    }

    @Test
    void testGetDocumentById_NotFound_ThrowsException() {
        when(documentRepository.findById(documentId)).thenReturn(Optional.empty());

        assertThrows(DocumentNotFoundException.class, () -> documentService.getDocumentById(documentId));
    }

    @Test
    void testDownloadDocument_Success() {
        Resource mockResource = new ByteArrayResource("content".getBytes());
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
        when(storageService.loadFileAsResource(document.getStorageReference())).thenReturn(mockResource);

        Resource result = documentService.downloadDocument(documentId);

        assertNotNull(result);
        assertTrue(result.exists());
    }

    @Test
    void testDownloadDocument_Archived_ThrowsException() {
        document.setStatus(DocumentStatus.ARCHIVED);
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));

        assertThrows(InvalidDocumentException.class, () -> documentService.downloadDocument(documentId));
    }

    @Test
    void testGetDocumentsByReferenceId_Success() {
        when(documentRepository.findByReferenceId(referenceId)).thenReturn(List.of(document));
        when(documentMapper.toResponseList(List.of(document))).thenReturn(List.of(documentResponse));

        List<DocumentResponse> results = documentService.getDocumentsByReferenceId(referenceId);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(referenceId, results.get(0).getReferenceId());
    }
}
