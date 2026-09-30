package com.healthinsurance.claims.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDocumentRequest {

    @NotNull(message = "Document ID from Document Service is required")
    private UUID documentId;

    @NotBlank(message = "Document type is required (e.g. MEDICAL_BILL, DISCHARGE_SUMMARY, PRESCRIPTION)")
    private String documentType;

    @NotBlank(message = "Document name is required")
    private String documentName;
}
