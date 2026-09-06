package com.healthinsurance.underwriting.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectUnderwritingRequest {
    @NotBlank(message = "A rejection reason is strictly required")
    private String reason;
    private String notes;
}