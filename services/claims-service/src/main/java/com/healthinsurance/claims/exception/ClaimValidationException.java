package com.healthinsurance.claims.exception;

import java.util.List;

public class ClaimValidationException extends RuntimeException {

    private final List<String> errors;

    public ClaimValidationException(String message) {
        super(message);
        this.errors = List.of(message);
    }

    public ClaimValidationException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
