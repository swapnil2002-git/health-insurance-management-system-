package com.healthinsurance.customer.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContactRequest {
    @NotBlank(message = "Contact type is required (e.g. EMAIL, PHONE)")
    private String contactType;
    @NotBlank(message = "Contact value is required")
    private String contactValue;
    private boolean isPrimary;
}