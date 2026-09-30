package com.healthinsurance.customer.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContactRequest {
    @NotBlank(message = "Contact type is required (e.g. EMAIL, PHONE)")
    @Pattern(regexp = "^(EMAIL|PHONE|MOBILE|WORK_PHONE|HOME_PHONE|Email|Phone|Mobile)$", message = "Contact type must be EMAIL, PHONE, or MOBILE")
    private String contactType;

    @NotBlank(message = "Contact value is required")
    @Size(min = 3, max = 100, message = "Contact value must be between 3 and 100 characters")
    private String contactValue;

    private boolean isPrimary;
}