package com.healthinsurance.claims.dto;

import com.healthinsurance.claims.domain.ValidationStatus;
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
public class ClaimValidationResponse {

    private UUID validationId;
    private UUID claimId;
    private String ruleName;
    private ValidationStatus status;
    private String message;
    private Instant validatedAt;
}
