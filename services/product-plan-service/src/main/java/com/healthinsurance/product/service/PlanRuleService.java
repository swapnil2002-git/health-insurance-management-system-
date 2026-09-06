package com.healthinsurance.product.service;

import com.healthinsurance.product.dto.request.PlanRuleRequest;
import com.healthinsurance.product.dto.response.RuleResponse;
import java.util.UUID;

public interface PlanRuleService {
    RuleResponse addCoverage(UUID planId, PlanRuleRequest request);
    RuleResponse addExclusion(UUID planId, PlanRuleRequest request);
    RuleResponse addDeductible(UUID planId, PlanRuleRequest request);
    RuleResponse addCopayment(UUID planId, PlanRuleRequest request);
    RuleResponse addRider(UUID planId, PlanRuleRequest request);
}