package com.healthinsurance.quotation.dto.response;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class QuoteVersionResponse {
    private UUID versionId;
    private Integer versionNumber;
    private BigDecimal premiumAtVersion;
    private String reason;
    private Instant createdAt;
}