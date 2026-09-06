package com.healthinsurance.product.service;
import com.healthinsurance.product.dto.request.PlanRequest;
import com.healthinsurance.product.dto.response.PlanResponse;
import java.util.UUID;

public interface PlanService {
    PlanResponse createPlan(PlanRequest request);
    PlanResponse getPlan(UUID planId);
    PlanResponse updatePlan(UUID planId, PlanRequest request);
}