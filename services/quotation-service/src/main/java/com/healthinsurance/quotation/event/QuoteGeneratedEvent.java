package com.healthinsurance.quotation.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuoteGeneratedEvent {
    private UUID quoteId;
    private UUID customerId;
    private UUID planId;
    private BigDecimal totalPremium;
    private String status;
}