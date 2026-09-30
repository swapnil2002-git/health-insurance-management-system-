package com.healthinsurance.product.service.impl;
import com.healthinsurance.product.dto.request.PlanRequest;
import com.healthinsurance.product.dto.response.PlanDetailResponse;
import com.healthinsurance.product.dto.response.PlanResponse;
import com.healthinsurance.product.dto.response.RuleResponse;
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

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    @Transactional(readOnly = true)
    public PlanDetailResponse getPlan(UUID planId) {
        InsurancePlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new PlanNotFoundException("Plan not found with ID: " + planId));

        PlanDetailResponse response = new PlanDetailResponse();
        response.setPlanId(plan.getPlanId());
        if (plan.getProduct() != null) {
            response.setProductId(plan.getProduct().getProductId());
            response.setProductName(plan.getProduct().getName());
        }
        response.setName(plan.getName());
        response.setDescription(plan.getDescription());

        // Map coverages
        if (plan.getPlanCoverages() != null) {
            response.setCoverages(plan.getPlanCoverages().stream()
                    .map(pc -> {
                        RuleResponse r = new RuleResponse();
                        r.setId(pc.getCoverage().getCoverageId());
                        r.setName(pc.getCoverage().getName());
                        r.setDescription(pc.getCoverage().getDescription());
                        return r;
                    }).collect(Collectors.toList()));
        } else {
            response.setCoverages(Collections.emptyList());
        }

        // Map deductibles
        if (plan.getPlanDeductibles() != null) {
            response.setDeductibles(plan.getPlanDeductibles().stream()
                    .map(pd -> {
                        RuleResponse r = new RuleResponse();
                        r.setId(pd.getDeductible().getDeductibleId());
                        r.setName(pd.getDeductible().getName());
                        r.setDescription(pd.getDeductible().getDescription());
                        return r;
                    }).collect(Collectors.toList()));
        } else {
            response.setDeductibles(Collections.emptyList());
        }

        // Map copayments
        if (plan.getPlanCopayments() != null) {
            response.setCopayments(plan.getPlanCopayments().stream()
                    .map(pc -> {
                        RuleResponse r = new RuleResponse();
                        r.setId(pc.getCopayment().getCopaymentId());
                        r.setName(pc.getCopayment().getName());
                        r.setDescription(pc.getCopayment().getDescription());
                        return r;
                    }).collect(Collectors.toList()));
        } else {
            response.setCopayments(Collections.emptyList());
        }

        // Map exclusions
        if (plan.getPlanExclusions() != null) {
            response.setExclusions(plan.getPlanExclusions().stream()
                    .map(pe -> {
                        RuleResponse r = new RuleResponse();
                        r.setId(pe.getExclusion().getExclusionId());
                        r.setName(pe.getExclusion().getName());
                        r.setDescription(pe.getExclusion().getDescription());
                        return r;
                    }).collect(Collectors.toList()));
        } else {
            response.setExclusions(Collections.emptyList());
        }

        // Map riders
        if (plan.getPlanRiders() != null) {
            response.setRiders(plan.getPlanRiders().stream()
                    .map(pr -> {
                        RuleResponse r = new RuleResponse();
                        r.setId(pr.getRider().getRiderId());
                        r.setName(pr.getRider().getName());
                        r.setDescription(pr.getRider().getDescription());
                        return r;
                    }).collect(Collectors.toList()));
        } else {
            response.setRiders(Collections.emptyList());
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponse> getAllPlans() {
        return planRepository.findAll().stream()
                .map(planMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponse> getPlansByProductId(UUID productId) {
        return planRepository.findByProduct_ProductId(productId).stream()
                .map(planMapper::toResponse)
                .collect(Collectors.toList());
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