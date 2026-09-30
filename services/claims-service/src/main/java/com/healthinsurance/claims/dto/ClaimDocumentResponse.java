package com.healthinsurance.claims.dto;

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
public class ClaimDocumentResponse {

    private UUID claimDocumentId;
    private UUID claimId;
    private UUID documentId;
    private String documentType;
    private String documentName;
    private Instant uploadedAt;
}
