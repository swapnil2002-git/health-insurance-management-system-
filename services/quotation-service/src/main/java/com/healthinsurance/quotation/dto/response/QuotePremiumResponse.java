package com.healthinsurance.quotation.dto.response;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class QuotePremiumResponse {
    private UUID quotePremiumId;
    private BigDecimal calculatedPremium;
    private String calculationDetails;
    private Instant calculatedAt;
}