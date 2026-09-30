package com.healthinsurance.underwriting.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RejectUnderwritingRequest {
    @NotBlank(message = "A rejection reason is strictly required")
    @Size(min = 3, max = 500, message = "Rejection reason must be between 3 and 500 characters")
    private String reason;

    @Size(max = 1000, message = "Rejection notes cannot exceed 1000 characters")
    private String notes;
}