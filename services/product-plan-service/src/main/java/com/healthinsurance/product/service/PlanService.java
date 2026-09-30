package com.healthinsurance.product.service;
import com.healthinsurance.product.dto.request.PlanRequest;
import com.healthinsurance.product.dto.response.PlanDetailResponse;
import com.healthinsurance.product.dto.response.PlanResponse;
import java.util.List;
import java.util.UUID;

public interface PlanService {
    PlanResponse createPlan(PlanRequest request);
    PlanDetailResponse getPlan(UUID planId);
    List<PlanResponse> getAllPlans();
    List<PlanResponse> getPlansByProductId(UUID productId);
    PlanResponse updatePlan(UUID planId, PlanRequest request);
}