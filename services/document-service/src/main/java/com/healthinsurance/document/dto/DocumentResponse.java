package com.healthinsurance.document.dto;

import com.healthinsurance.document.domain.DocumentStatus;
import com.healthinsurance.document.domain.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {

    private UUID documentId;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private String storageReference;
    private DocumentType documentType;
    private UUID referenceId;
    private DocumentStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
