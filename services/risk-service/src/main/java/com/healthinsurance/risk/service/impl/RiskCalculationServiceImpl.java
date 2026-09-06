package com.healthinsurance.risk.service.impl;

import com.healthinsurance.risk.entity.RiskFactor;
import com.healthinsurance.risk.enums.RiskClassification;
import com.healthinsurance.risk.service.RiskCalculationResult;
import com.healthinsurance.risk.service.RiskCalculationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
public class RiskCalculationServiceImpl implements RiskCalculationService {

    @Override
    public RiskCalculationResult calculateRisk(Set<RiskFactor> factors) {
        log.info("Executing extensible Risk Calculation Engine...");

        // =========================================================================
        // TEMPORARY BUSINESS RULE PLACEHOLDER (Do NOT invent complex medical rules)
        // =========================================================================
        int score = 10; // Base baseline risk

        if (factors != null) {
            // Placeholder logic: Add 15 points of risk for every factor reported
            score += (factors.size() * 15);
        }

        RiskClassification classification;
        if (score < 25) {
            classification = RiskClassification.LOW;
        } else if (score <= 50) {
            classification = RiskClassification.MEDIUM;
        } else if (score <= 75) {
            classification = RiskClassification.HIGH;
        } else {
            classification = RiskClassification.VERY_HIGH;
        }
        
        log.info("Calculated Risk Score: {}, Classification: {}", score, classification);
        return new RiskCalculationResult(score, classification);
    }
}