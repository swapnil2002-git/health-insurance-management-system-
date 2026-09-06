package com.healthinsurance.premium.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuoteResponseDto {
    private UUID quoteId;
    private UUID customerId;
    private UUID planId;
    private String quoteNumber;
    private String status;
    private BigDecimal totalPremium;
}