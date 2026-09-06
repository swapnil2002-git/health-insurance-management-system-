package com.healthinsurance.policy.dto.response;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;
@Data
public class PolicyBeneficiaryResponse { private UUID policyBeneficiaryId; private String beneficiaryName; private String relationship; private BigDecimal percentage; }