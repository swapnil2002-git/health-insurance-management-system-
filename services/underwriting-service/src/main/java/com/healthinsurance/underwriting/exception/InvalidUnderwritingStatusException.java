package com.healthinsurance.underwriting.exception;

public class InvalidUnderwritingStatusException extends RuntimeException {
    public InvalidUnderwritingStatusException(String message) {
        super(message);
    }
}