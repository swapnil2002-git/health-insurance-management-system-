package com.healthinsurance.product.exception;
public class DeductibleNotFoundException extends RuntimeException {
    public DeductibleNotFoundException(String message) {
        super(message);
    }
}