package com.healthinsurance.policy.client.dto;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class QuoteDto {
    private UUID quoteId;
    private UUID planId;
    private List<QuoteMemberDto> members;
}