package com.healthinsurance.underwriting.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReferUnderwritingRequest {
    @NotBlank(message = "A referral reason is strictly required")
    @Size(min = 3, max = 500, message = "Referral reason must be between 3 and 500 characters")
    private String reason;

    @Size(max = 1000, message = "Referral notes cannot exceed 1000 characters")
    private String notes;
}