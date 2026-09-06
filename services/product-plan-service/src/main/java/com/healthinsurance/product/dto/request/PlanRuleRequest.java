package com.healthinsurance.product.dto.request;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class PlanRuleRequest {
    @NotNull(message = "Rule ID is required")
    private UUID ruleId;
}