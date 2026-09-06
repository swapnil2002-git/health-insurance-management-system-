package com.healthinsurance.underwriting.dto.request;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class CreateUnderwritingCaseRequest {
    @NotNull(message = "Quote ID is required")
    private UUID quoteId;

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    @NotNull(message = "Assessment ID is required")
    private UUID assessmentId;
}