package com.healthinsurance.policy.dto.response;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;
@Data
public class PolicyCoverageResponse { private UUID policyCoverageId; private String coverageName; private BigDecimal coverageAmount; private BigDecimal deductible; }