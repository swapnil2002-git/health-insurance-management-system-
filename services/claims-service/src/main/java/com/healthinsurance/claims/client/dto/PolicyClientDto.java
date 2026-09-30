package com.healthinsurance.claims.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolicyClientDto {
    private UUID policyId;
    private String policyNumber;
    private UUID customerId;
    private UUID planId;
    private UUID quoteId;
    private String status;
    private Instant effectiveDate;
    private Instant expiryDate;
    private List<PolicyMemberClientDto> members;
    private List<PolicyCoverageClientDto> coverages;

    @Data
    public static class PolicyMemberClientDto {
        private UUID policyMemberId;
        private UUID memberId;
    }

    @Data
    public static class PolicyCoverageClientDto {
        private UUID policyCoverageId;
        private String coverageName;
        private BigDecimal coverageAmount;
        private BigDecimal deductible;
    }
}
