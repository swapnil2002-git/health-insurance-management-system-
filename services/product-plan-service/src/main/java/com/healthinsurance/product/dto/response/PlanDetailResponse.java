package com.healthinsurance.product.dto.response;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class PlanDetailResponse {
    private UUID planId;
    private UUID productId;
    private String productName;
    private String name;
    private String description;
    private List<RuleResponse> coverages;
    private List<RuleResponse> deductibles;
    private List<RuleResponse> copayments;
    private List<RuleResponse> exclusions;
    private List<RuleResponse> riders;
}