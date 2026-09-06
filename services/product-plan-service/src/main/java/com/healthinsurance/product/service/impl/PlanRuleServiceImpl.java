package com.healthinsurance.product.service.impl;

import com.healthinsurance.product.dto.request.PlanRuleRequest;
import com.healthinsurance.product.dto.response.RuleResponse;
import com.healthinsurance.product.entity.*;
import com.healthinsurance.product.exception.*;
import com.healthinsurance.product.mapper.RuleMapper;
import com.healthinsurance.product.repository.*;
import com.healthinsurance.product.service.PlanRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlanRuleServiceImpl implements PlanRuleService {

    private final InsurancePlanRepository planRepo;
    private final RuleMapper ruleMapper;

    private final CoverageRepository coverageRepo;
    private final PlanCoverageRepository planCoverageRepo;

    private final ExclusionRepository exclusionRepo;
    private final PlanExclusionRepository planExclusionRepo;

    private final DeductibleRepository deductibleRepo;
    private final PlanDeductibleRepository planDeductibleRepo;

    private final CopaymentRepository copaymentRepo;
    private final PlanCopaymentRepository planCopaymentRepo;

    private final RiderRepository riderRepo;
    private final PlanRiderRepository planRiderRepo;

    private InsurancePlan getPlan(UUID planId) {
        return planRepo.findById(planId).orElseThrow(() -> new PlanNotFoundException("Plan not found: " + planId));
    }

    @Override
    @Transactional
    public RuleResponse addCoverage(UUID planId, PlanRuleRequest request) {
        InsurancePlan plan = getPlan(planId);
        Coverage coverage = coverageRepo.findById(request.getRuleId())
                .orElseThrow(() -> new CoverageNotFoundException("Coverage not found: " + request.getRuleId()));

        boolean exists = planCoverageRepo.findByPlan_PlanId(planId).stream()
                .anyMatch(pc -> pc.getCoverage().getCoverageId().equals(coverage.getCoverageId()));
        if (exists) throw new DuplicateRuleException("Coverage already exists in this plan.");

        PlanCoverage pc = new PlanCoverage();
        pc.setPlan(plan);
        pc.setCoverage(coverage);
        return ruleMapper.toCoverageResponse(planCoverageRepo.save(pc));
    }

    @Override
    @Transactional
    public RuleResponse addExclusion(UUID planId, PlanRuleRequest request) {
        InsurancePlan plan = getPlan(planId);
        Exclusion exclusion = exclusionRepo.findById(request.getRuleId())
                .orElseThrow(() -> new ExclusionNotFoundException("Exclusion not found: " + request.getRuleId()));

        boolean exists = planExclusionRepo.findByPlan_PlanId(planId).stream()
                .anyMatch(pe -> pe.getExclusion().getExclusionId().equals(exclusion.getExclusionId()));
        if (exists) throw new DuplicateRuleException("Exclusion already exists in this plan.");

        PlanExclusion pe = new PlanExclusion();
        pe.setPlan(plan);
        pe.setExclusion(exclusion);
        return ruleMapper.toExclusionResponse(planExclusionRepo.save(pe));
    }

    @Override
    @Transactional
    public RuleResponse addDeductible(UUID planId, PlanRuleRequest request) {
        InsurancePlan plan = getPlan(planId);
        Deductible deductible = deductibleRepo.findById(request.getRuleId())
                .orElseThrow(() -> new DeductibleNotFoundException("Deductible not found: " + request.getRuleId()));

        boolean exists = planDeductibleRepo.findByPlan_PlanId(planId).stream()
                .anyMatch(pd -> pd.getDeductible().getDeductibleId().equals(deductible.getDeductibleId()));
        if (exists) throw new DuplicateRuleException("Deductible already exists in this plan.");

        PlanDeductible pd = new PlanDeductible();
        pd.setPlan(plan);
        pd.setDeductible(deductible);
        return ruleMapper.toDeductibleResponse(planDeductibleRepo.save(pd));
    }

    @Override
    @Transactional
    public RuleResponse addCopayment(UUID planId, PlanRuleRequest request) {
        InsurancePlan plan = getPlan(planId);
        Copayment copayment = copaymentRepo.findById(request.getRuleId())
                .orElseThrow(() -> new CopaymentNotFoundException("Copayment not found: " + request.getRuleId()));

        boolean exists = planCopaymentRepo.findByPlan_PlanId(planId).stream()
                .anyMatch(pc -> pc.getCopayment().getCopaymentId().equals(copayment.getCopaymentId()));
        if (exists) throw new DuplicateRuleException("Copayment already exists in this plan.");

        PlanCopayment pc = new PlanCopayment();
        pc.setPlan(plan);
        pc.setCopayment(copayment);
        return ruleMapper.toCopaymentResponse(planCopaymentRepo.save(pc));
    }

    @Override
    @Transactional
    public RuleResponse addRider(UUID planId, PlanRuleRequest request) {
        InsurancePlan plan = getPlan(planId);
        Rider rider = riderRepo.findById(request.getRuleId())
                .orElseThrow(() -> new RiderNotFoundException("Rider not found: " + request.getRuleId()));

        boolean exists = planRiderRepo.findByPlan_PlanId(planId).stream()
                .anyMatch(pr -> pr.getRider().getRiderId().equals(rider.getRiderId()));
        if (exists) throw new DuplicateRuleException("Rider already exists in this plan.");

        PlanRider pr = new PlanRider();
        pr.setPlan(plan);
        pr.setRider(rider);
        return ruleMapper.toRiderResponse(planRiderRepo.save(pr));
    }
}