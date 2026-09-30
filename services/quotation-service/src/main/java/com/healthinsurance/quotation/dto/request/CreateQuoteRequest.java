package com.healthinsurance.quotation.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class CreateQuoteRequest {
    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    @NotNull(message = "Plan ID is required")
    private UUID planId;

    @NotEmpty(message = "At least one member is required for a quote")
    @jakarta.validation.constraints.Size(min = 1, max = 20, message = "Quotation can contain between 1 and 20 members")
    @Valid
    private List<QuoteMemberRequest> members;
}