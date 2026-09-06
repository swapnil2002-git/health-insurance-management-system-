package com.healthinsurance.risk.exception;

public class InvalidAssessmentStatusException extends RuntimeException {
    public InvalidAssessmentStatusException(String message) {
        super(message);
    }
}