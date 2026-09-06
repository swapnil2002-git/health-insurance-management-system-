package com.healthinsurance.risk.service;

import com.healthinsurance.risk.entity.RiskFactor;
import java.util.Set;

public interface RiskCalculationService {
    RiskCalculationResult calculateRisk(Set<RiskFactor> factors);
}