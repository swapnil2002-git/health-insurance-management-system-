package com.healthinsurance.underwriting.exception;
public class UnderwritingCaseNotFoundException extends RuntimeException {
    public UnderwritingCaseNotFoundException(String message) {
        super(message);
    }
}