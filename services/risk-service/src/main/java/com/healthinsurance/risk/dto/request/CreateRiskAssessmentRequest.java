package com.healthinsurance.risk.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class CreateRiskAssessmentRequest {
    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    @NotNull(message = "Quote ID is required")
    private UUID quoteId;

    @NotEmpty(message = "At least one risk factor (questionnaire answer) is required")
    @Valid
    private List<RiskFactorRequest> factors;
}