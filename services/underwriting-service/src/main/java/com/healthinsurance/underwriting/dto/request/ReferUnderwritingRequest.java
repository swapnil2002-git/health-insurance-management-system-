package com.healthinsurance.underwriting.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReferUnderwritingRequest {
    @NotBlank(message = "A referral reason is strictly required")
    private String reason;
    private String notes;
}