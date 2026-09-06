package com.healthinsurance.product.exception;
public class CoverageNotFoundException extends RuntimeException {
    public CoverageNotFoundException(String message) {
        super(message);
    }
}