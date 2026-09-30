package com.healthinsurance.claims.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimServiceRequest {

    @NotBlank(message = "Service code is required")
    @jakarta.validation.constraints.Size(min = 2, max = 30, message = "Service code must be between 2 and 30 characters")
    @jakarta.validation.constraints.Pattern(regexp = "^[A-Za-z0-9-_]+$", message = "Service code must be alphanumeric (e.g. CPT code)")
    private String serviceCode;

    @NotBlank(message = "Service description is required")
    @jakarta.validation.constraints.Size(min = 2, max = 255, message = "Service description must be between 2 and 255 characters")
    private String serviceDescription;

    @NotNull(message = "Service date is required")
    @jakarta.validation.constraints.PastOrPresent(message = "Service date cannot be in the future")
    private LocalDate serviceDate;

    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be greater than zero")
    private BigDecimal unitPrice;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be at least 1")
    @Builder.Default
    private Integer quantity = 1;
}
