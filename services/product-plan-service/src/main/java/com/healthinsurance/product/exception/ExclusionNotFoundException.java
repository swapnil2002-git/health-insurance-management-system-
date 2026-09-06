package com.healthinsurance.product.exception;
public class ExclusionNotFoundException extends RuntimeException {
    public ExclusionNotFoundException(String message) {
        super(message);
    }
}