package com.healthinsurance.policy.client.dto;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PlanCoverageDto {
    @JsonAlias({"name", "coverageName"})
    private String coverageName;
    private BigDecimal coverageAmount;
    private BigDecimal deductible;

    public String getCoverageName() {
        return (coverageName != null && !coverageName.isBlank()) ? coverageName : "Standard Health Coverage";
    }

    public BigDecimal getCoverageAmount() {
        return (coverageAmount != null) ? coverageAmount : BigDecimal.valueOf(500000.00);
    }

    public BigDecimal getDeductible() {
        return (deductible != null) ? deductible : BigDecimal.ZERO;
    }
}