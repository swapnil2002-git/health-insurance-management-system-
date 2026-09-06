package com.healthinsurance.premium.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanResponseDto {
    private UUID planId;
    private UUID productId;
    private String name;
    private String description;
    private BigDecimal basePremium;
}