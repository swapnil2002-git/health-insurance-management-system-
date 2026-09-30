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
public class ClaimDiagnosisResponse {

    private UUID diagnosisId;
    private UUID claimId;
    private String diagnosisCode;
    private String description;
    private boolean primary;
    private Instant createdAt;
}
