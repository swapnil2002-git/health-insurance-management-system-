package com.healthinsurance.product.exception;
public class DuplicateRuleException extends RuntimeException {
    public DuplicateRuleException(String message) {
        super(message);
    }
}