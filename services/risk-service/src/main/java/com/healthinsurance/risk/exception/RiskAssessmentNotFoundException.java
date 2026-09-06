package com.healthinsurance.risk.exception;

public class RiskAssessmentNotFoundException extends RuntimeException {
    public RiskAssessmentNotFoundException(String message) {
        super(message);
    }
}