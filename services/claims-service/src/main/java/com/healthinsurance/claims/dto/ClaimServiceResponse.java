package com.healthinsurance.claims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimServiceResponse {

    private UUID claimServiceId;
    private UUID claimId;
    private String serviceCode;
    private String serviceDescription;
    private LocalDate serviceDate;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalAmount;
    private Instant createdAt;
}
