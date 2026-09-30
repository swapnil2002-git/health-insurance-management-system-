package com.healthinsurance.claims.dto;

import com.healthinsurance.claims.domain.ClaimType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimCreateRequest {

    @NotNull(message = "Policy ID is required")
    private UUID policyId;

    @NotNull(message = "Member ID is required")
    private UUID memberId;

    @NotNull(message = "Provider ID is required")
    private UUID providerId;

    @NotNull(message = "Claim type is required")
    private ClaimType claimType;

    @NotNull(message = "Service date is required")
    @jakarta.validation.constraints.PastOrPresent(message = "Service date cannot be in the future")
    private LocalDate serviceDate;

    private LocalDate admissionDate;

    private LocalDate dischargeDate;

    @NotNull(message = "Total claim amount is required")
    @Positive(message = "Total claim amount must be greater than zero")
    @jakarta.validation.constraints.DecimalMin(value = "0.01", message = "Total claim amount must be at least 0.01")
    private BigDecimal totalClaimAmount;

    @jakarta.validation.constraints.Size(max = 1000, message = "Remarks cannot exceed 1000 characters")
    private String remarks;

    @Valid
    @jakarta.validation.constraints.Size(max = 50, message = "Cannot exceed 50 service lines")
    private List<ClaimServiceRequest> serviceLines;

    @Valid
    @jakarta.validation.constraints.Size(max = 20, message = "Cannot exceed 20 diagnoses")
    private List<ClaimDiagnosisRequest> diagnoses;
}
