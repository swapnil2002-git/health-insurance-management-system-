package com.healthinsurance.product.service.impl;
import com.healthinsurance.product.dto.request.PlanRequest;
import com.healthinsurance.product.dto.response.PlanResponse;
import com.healthinsurance.product.entity.InsurancePlan;
import com.healthinsurance.product.entity.InsuranceProduct;
import com.healthinsurance.product.exception.PlanNotFoundException;
import com.healthinsurance.product.exception.ProductNotFoundException;
import com.healthinsurance.product.mapper.PlanMapper;
import com.healthinsurance.product.repository.InsurancePlanRepository;
import com.healthinsurance.product.repository.InsuranceProductRepository;
import com.healthinsurance.product.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {
    private final InsurancePlanRepository planRepository;
    private final InsuranceProductRepository productRepository;
    private final PlanMapper planMapper;

    @Override
    @Transactional
    public PlanResponse createPlan(PlanRequest request) {
        InsuranceProduct product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Cannot create plan. Product not found: " + request.getProductId()));
        
        InsurancePlan plan = planMapper.toEntity(request);
        plan.setProduct(product);
        return planMapper.toResponse(planRepository.save(plan));
    }

    @Override
    public PlanResponse getPlan(UUID planId) {
        InsurancePlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new PlanNotFoundException("Plan not found with ID: " + planId));
        return planMapper.toResponse(plan);
    }

    @Override
    @Transactional
    public PlanResponse updatePlan(UUID planId, PlanRequest request) {
        InsurancePlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new PlanNotFoundException("Plan not found with ID: " + planId));
                
        if (!plan.getProduct().getProductId().equals(request.getProductId())) {
            InsuranceProduct newProduct = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + request.getProductId()));
            plan.setProduct(newProduct);
        }
        
        planMapper.updateEntityFromRequest(request, plan);
        return planMapper.toResponse(planRepository.save(plan));
    }
}