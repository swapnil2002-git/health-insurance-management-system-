package com.healthinsurance.risk.service;

import com.healthinsurance.risk.enums.RiskClassification;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RiskCalculationResult {
    private int score;
    private RiskClassification classification;
}