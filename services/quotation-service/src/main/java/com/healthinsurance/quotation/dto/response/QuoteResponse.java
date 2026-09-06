package com.healthinsurance.quotation.dto.response;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class QuoteResponse {
    private UUID quoteId;
    private UUID customerId;
    private UUID planId;
    private String quoteNumber;
    private String status;
    private BigDecimal totalPremium;
    private Instant createdAt;
    private Instant expiresAt;
    private Instant acceptedAt;
    private Instant rejectedAt;
    
    private List<QuoteMemberResponse> members;
    private List<QuotePremiumResponse> premiums;
    private List<QuoteVersionResponse> versions;
}