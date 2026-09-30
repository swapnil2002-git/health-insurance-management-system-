package com.healthinsurance.claims.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDiagnosisRequest {

    @NotBlank(message = "Diagnosis code is required")
    @jakarta.validation.constraints.Size(min = 2, max = 20, message = "Diagnosis code must be between 2 and 20 characters")
    @jakarta.validation.constraints.Pattern(regexp = "^[A-Za-z0-9.]+$", message = "Diagnosis code must be alphanumeric (e.g. ICD-10 format like E11.9)")
    private String diagnosisCode;

    @NotBlank(message = "Diagnosis description is required")
    @jakarta.validation.constraints.Size(min = 2, max = 255, message = "Diagnosis description must be between 2 and 255 characters")
    private String description;

    @Builder.Default
    private boolean primary = false;
}
